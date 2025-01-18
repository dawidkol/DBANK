package pl.dk.notification_service.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.dk.notification_service.httpClient.UserServiceFeignClient;
import pl.dk.notification_service.kafka.consumer.dtos.LoanScheduleReminder;
import pl.dk.notification_service.kafka.consumer.dtos.UserDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static pl.dk.notification_service.kafka.KafkaConstants.LOAN_SERVICE_LOAN_REMINDER;
import static pl.dk.notification_service.kafka.KafkaConstants.TOPIC_REGISTRATION;

@SpringBootTest
@EmbeddedKafka(topics = {TOPIC_REGISTRATION, LOAN_SERVICE_LOAN_REMINDER})
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class EmailNotificationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, UserDto> userDtoKafkaTemplate;

    @Autowired
    private KafkaTemplate<String, LoanScheduleReminder> loanScheduleReminderKafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @MockitoBean
    private UserServiceFeignClient userServiceFeignClient;

    @MockitoSpyBean
    private EmailNotification emailNotification;


    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    @DisplayName("It should consume UserDto record successfully")
    void itShouldConsumeUserDtoRecordSuccessfully() throws InterruptedException {
        // Given
        UserDto userDto = UserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("dkcodepro@gmail.com")
                .phone("+48666666666")
                .userId(UUID.randomUUID().toString())
                .build();
        userDtoKafkaTemplate.send(TOPIC_REGISTRATION, userDto);

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
        verify(emailNotification, times(1))
                .sendUserRegistrationEmail(isA(ConsumerRecord.class));

    }

    @Test
    @DisplayName("It should consume LoanReminder record successfully")
    void itShouldConsumeLoanReminderRecordSuccessfully() throws InterruptedException {
        // Given
        LoanScheduleReminder loanScheduleReminder = LoanScheduleReminder.builder()
                .loanScheduleId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .installment(BigDecimal.valueOf(1000))
                .deadline(LocalDate.now().plusDays(3))
                .paymentStatus("UNPAID")
                .build();
        loanScheduleReminderKafkaTemplate.send(LOAN_SERVICE_LOAN_REMINDER, loanScheduleReminder);

        when(userServiceFeignClient.getUserById(loanScheduleReminder.userId()))
                .thenReturn(ResponseEntity.ok(UserDto.builder().email("dkcodepro@gmail.com").build()));

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
        assertAll(() -> {
            verify(userServiceFeignClient, times(1)).getUserById(loanScheduleReminder.userId());
            verify(emailNotification, atLeast(1))
                    .sendLoanReminderEmail(isA(ConsumerRecord.class), isA(Acknowledgment.class));
        });
    }

    @Test
    @DisplayName("It should consume LoanReminder record successfully but User-Service returns 404 NOT_FOUND")
    void itShouldConsumeLoanReminderRecordButUserServiceReturns404NotFound() throws InterruptedException {
        // Given
        LoanScheduleReminder loanScheduleReminder = LoanScheduleReminder.builder()
                .loanScheduleId(UUID.randomUUID().toString())
                .userId(UUID.randomUUID().toString())
                .installment(BigDecimal.valueOf(1000))
                .deadline(LocalDate.now().plusDays(3))
                .paymentStatus("UNPAID")
                .build();
        loanScheduleReminderKafkaTemplate.send(LOAN_SERVICE_LOAN_REMINDER, loanScheduleReminder);

        when(userServiceFeignClient.getUserById(loanScheduleReminder.userId()))
                .thenReturn(ResponseEntity.notFound().build());

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        // Then
        assertAll(() -> {
            verify(userServiceFeignClient, times(1)).getUserById(loanScheduleReminder.userId());
            verify(emailNotification, atLeast(1))
                    .sendLoanReminderEmail(isA(ConsumerRecord.class), isA(Acknowledgment.class));
        });
    }
}