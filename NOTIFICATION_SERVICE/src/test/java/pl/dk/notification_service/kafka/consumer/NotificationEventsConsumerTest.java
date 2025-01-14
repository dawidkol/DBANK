package pl.dk.notification_service.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.dk.notification_service.kafka.KafkaConstants.TOPIC_REGISTRATION;

@SpringBootTest
@EmbeddedKafka(topics = TOPIC_REGISTRATION)
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class NotificationEventsConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, pl.dk.notification_service.kafka.consumer.SaveUserDto> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @MockitoSpyBean
    private EmailNotification notificationEventsConsumerSpy;


    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    @DisplayName("It should send Notification successfully")
    void itShouldSendNotificationSuccessfully() throws InterruptedException {
        // Given
        pl.dk.notification_service.kafka.consumer.SaveUserDto saveUserDto = pl.dk.notification_service.kafka.consumer.SaveUserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("dkcodepro@gmail.com")
                .phone("+48666666666")
                .password("securePassword123")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .build();
        kafkaTemplate.send(TOPIC_REGISTRATION, saveUserDto);

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
        verify(notificationEventsConsumerSpy, times(1))
                .onUserServiceMessage(isA(ConsumerRecord.class));

    }
}