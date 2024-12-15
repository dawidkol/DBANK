package pl.dk.user_service.user;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.dk.user_service.kafka.KafkaConstants;
import pl.dk.user_service.user.dto.UserDto;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static pl.dk.user_service.kafka.KafkaConstants.USER_DTO_TRUSTED_PACKAGE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@EmbeddedKafka(topics = KafkaConstants.TOPIC_REGISTRATION)
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, UserDto> consumer;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        JsonDeserializer<UserDto> userDtoJsonDeserializer = new JsonDeserializer<>();
        userDtoJsonDeserializer.addTrustedPackages(USER_DTO_TRUSTED_PACKAGE);
        consumer = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), userDtoJsonDeserializer).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @DisplayName("User controller CRUD endpoints integration test")
    void userControllerCrudEndpointsIntegrationTest() throws Exception {
        String saveUserDtoJsonValid = """
                {
                  "firstName": "John",
                  "lastName": "Doe",
                  "email": "john.doe@test.pl",
                  "phone": "+48666999666",
                  "password": "securepassword123",
                  "dateOfBirth": "1990-01-01"
                }
                """;

        // 1. User wants to register | expected status code 201
        ResultActions registerSuccessResultAction = mockMvc.perform(MockMvcRequestBuilders.post("/users").contentType(APPLICATION_JSON_VALUE)
                        .content(saveUserDtoJsonValid))
                .andExpect(MockMvcResultMatchers.status().is(CREATED.value()));

        ConsumerRecords<String, UserDto> records = KafkaTestUtils.getRecords(consumer);
        assertThat(records.count()).isEqualTo(1);


        // 2. User wants to register with the same email and phone | expected status code 209
        mockMvc.perform(MockMvcRequestBuilders.post("/users").contentType(APPLICATION_JSON_VALUE)
                        .content(saveUserDtoJsonValid))
                .andExpect(MockMvcResultMatchers.status().is(CONFLICT.value()));

        // 3. User wants to retrieve user info by providing userId | expected status code 200
        String location = registerSuccessResultAction.andReturn()
                .getResponse()
                .getHeader(LOCATION);

        String userId = null;
        if (location != null) {
            String[] split = location.split("/");
            userId = split[split.length - 1];
        }
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}", userId))
                .andExpect(MockMvcResultMatchers.status().is(OK.value()));

        // 4. User wants to retrieve user info by providing not existing userId | expected status code 204
        String notExistingUserId = "80d841f4-20da-4882-83d2-62e1b101fde9";
        mockMvc.perform(MockMvcRequestBuilders.get("/users/{userId}", notExistingUserId))
                .andExpect(MockMvcResultMatchers.status().is(NOT_FOUND.value()));

        // 5. User wants to update user data | expected status code 204
        String updateUserDataJson = """
                {
                  "firstName": "Updated John",
                  "lastName": "Updated Doe",
                  "email": "updated.johndoe@test.pl",
                  "phone": "+48111111111",
                  "password": "Updated securepassword123",
                  "dateOfBirth": "1980-05-05"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.patch("/users/{userId}", userId)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(updateUserDataJson))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 6. User wants to delete user by providing userId | expected status code 204
        mockMvc.perform(MockMvcRequestBuilders.delete("/users/{userId}", userId))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

    }

}
