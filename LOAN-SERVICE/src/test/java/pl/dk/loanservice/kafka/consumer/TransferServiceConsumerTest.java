package pl.dk.loanservice.kafka.consumer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.KafkaUtils;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.dk.loanservice.enums.TransferStatus;
import pl.dk.loanservice.httpClient.dtos.UserDto;
import pl.dk.loanservice.kafka.consumer.dtos.LoanTransferDto;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pl.dk.loanservice.kafka.KafkaConstants.*;

@SpringBootTest
@EmbeddedKafka(topics = TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS )
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class TransferServiceConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, LoanTransferDto> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @MockitoSpyBean
    private TransferServiceConsumer transferServiceConsumer;

    @MockitoBean
    private LoanRepository loanRepository;

    private Consumer<String, LoanTransferDto> consumer;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
        Map<String, Object> configs = KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        JsonDeserializer<LoanTransferDto> loanTransferDeserializer = new JsonDeserializer<>();
        loanTransferDeserializer.addTrustedPackages(LOAN_SERVICE_TRUSTED_PACKAGE);


        consumer = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(),
                loanTransferDeserializer).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
    }

    @Test
    void itShouldConsumeRecordSuccessfully() {
        // Given
        LoanTransferDto loanTransferDto = LoanTransferDto.builder()
                .id(UUID.randomUUID().toString())
                .transferDate(LocalDateTime.now())
                .transferStatus(TransferStatus.COMPLETED)
                .build();
        kafkaTemplate.send(TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS, loanTransferDto);

        // When
        ConsumerRecords<String, LoanTransferDto> records = KafkaTestUtils.getRecords(consumer);

        // Then
        assertEquals(1, records.count());
    }
}
