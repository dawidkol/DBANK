package pl.dk.notification_service.failed_message.loan_schedule;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import pl.dk.notification_service.kafka.consumer.dtos.LoanScheduleReminder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;
import static pl.dk.notification_service.kafka.KafkaConstants.LOAN_SERVICE_LOAN_REMINDER;

class LoanReminderRetryServiceTest {


    @Mock
    private LoanReminderRepository loanReminderRepository;
    @Mock
    private KafkaTemplate<String, LoanScheduleReminder> loanScheduleReminderKafkaTemplate;
    private AutoCloseable autoCloseable;
    private LoanReminderRetryService underTest;

    LoanReminderRetry loanReminderRetry;
    LoanScheduleReminder value;
    ConsumerRecord<String, LoanScheduleReminder> record;

    @BeforeEach
    void setUp() {
        autoCloseable = openMocks(this);
        underTest = new LoanReminderRetryServiceImpl(loanReminderRepository, loanScheduleReminderKafkaTemplate);

        String loanSchedulerId = UUID.randomUUID().toString();
        value = LoanScheduleReminder.builder()
                .loanScheduleId(loanSchedulerId)
                .paymentStatus("UNPAID")
                .deadline(LocalDate.now().minusDays(3))
                .installment(BigDecimal.valueOf(1000))
                .userId(UUID.randomUUID().toString())
                .build();

        record = new ConsumerRecord<>(
                LOAN_SERVICE_LOAN_REMINDER,
                1,
                1,
                loanSchedulerId,
                value);

        loanReminderRetry = LoanReminderRetry.builder()
                .id(UUID.randomUUID().toString())
                .loanScheduleId(record.key())
                .sent(false)
                .paymentStatus("UNPAID")
                .deadline(value.deadline())
                .installment(value.installment())
                .userId(value.userId())
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should save LoanReminderRetry object successfully")
    void itShouldSaveLoanReminderRetryObjectSuccessfully() {
        // Given


        when(loanReminderRepository.save(loanReminderRetry)).thenReturn(any());

        // When
        underTest.save(record);

        // Then
        assertAll(() -> {
            verify(loanReminderRepository, times(1)).save(any());
        });
    }

    @Test
    @DisplayName("It should send failed messages to kafka topic")
    void itShouldRetryFailedLoanReminders() {
        // Given
        LocalDate before = LocalDate.now();
        when(loanReminderRepository.findAllByDeadlineIsLessThanEqualAndSent(
                before,
                false,
                PageRequest.of(0, 1000)))
                .thenReturn(new PageImpl<>(List.of(loanReminderRetry)));

        // When
        underTest.retryFailedLoanReminders();

        // Then
        assertAll(() -> {
            verify(loanReminderRepository, times(1))
                    .findAllByDeadlineIsLessThanEqualAndSent(before, false, PageRequest.of(0, 1000));
            verify(loanScheduleReminderKafkaTemplate, times(1))
                    .send(anyString(), anyString(), any(LoanScheduleReminder.class));
            verify(loanReminderRepository, times(1)).saveAll(anyCollection());
        });
    }
}