package pl.dk.loanservice.kafka.producer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.dk.loanservice.loan_schedule.LoanSchedule;
import pl.dk.loanservice.loan_schedule.LoanScheduleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static pl.dk.loanservice.enums.PaymentStatus.UNPAID;
import static pl.dk.loanservice.kafka.KafkaConstants.LOAN_SERVICE_LOAN_REMINDER;

@SpringBootTest(webEnvironment = RANDOM_PORT,
        properties = {"scheduler.payment-status.loan-schedule-reminder=0/1 * * * * *"})
@EmbeddedKafka(topics = LOAN_SERVICE_LOAN_REMINDER)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
class NotificationServiceProducerTest {

    @MockitoBean
    private LoanScheduleRepository loanScheduleRepository;

    @MockitoSpyBean
    private NotificationServiceProducer notificationServiceProducer;


    @Test
    @DisplayName("It should invoke scheduled method and send message successfully to kafka topic")
    void itShouldInvokeScheduledMethodAndSendMessageSuccessfullyToKafkaTopic() {
        LoanSchedule loanSchedule = LoanSchedule.builder()
                .id(UUID.randomUUID().toString())
                .paymentStatus(UNPAID)
                .deadline(LocalDate.now().plusDays(3))
                .paymentDate(LocalDate.now())
                .installment(BigDecimal.valueOf(1000))
                .build();
        when(loanScheduleRepository.findAllByDeadlineBeforeAndPaymentStatusIn(
                any(LocalDate.class),
                anyCollection()))
                        .thenReturn(List.of(loanSchedule));
        assertAll(() ->
                await().untilAsserted(() -> {
                    verify(loanScheduleRepository,times(1))
                            .findAllByDeadlineBeforeAndPaymentStatusIn(any(LocalDate.class),
                                    anyCollection());
                    verify(notificationServiceProducer, atLeastOnce()).produceReminderMessage();
                }));
    }
}