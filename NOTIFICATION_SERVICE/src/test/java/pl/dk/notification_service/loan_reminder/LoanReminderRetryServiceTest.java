package pl.dk.notification_service.loan_reminder;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.kafka.core.KafkaTemplate;
import pl.dk.notification_service.kafka.consumer.dtos.LoanScheduleReminder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
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


    @BeforeEach
    void setUp() {
        autoCloseable = openMocks(this);
        underTest = new LoanReminderRetryServiceImpl(loanReminderRepository, loanScheduleReminderKafkaTemplate);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should save LoanReminderRetry object successfully")
    void itShouldSaveLoanReminderRetryObjectSuccessfully() {
        // Given
        String key = UUID.randomUUID().toString();
        LoanScheduleReminder value = LoanScheduleReminder.builder()
                .id(key)
                .paymentStatus("UNPAID")
                .deadline(LocalDate.now().minusDays(3))
                .installment(BigDecimal.valueOf(1000))
                .userId(UUID.randomUUID().toString())
                .build();

        ConsumerRecord<String, LoanScheduleReminder> record = new ConsumerRecord<>(
                LOAN_SERVICE_LOAN_REMINDER,
                1,
                1,
                key,
                value);

        LoanReminderRetry loanReminderRetry = LoanReminderRetry.builder()
                .id(record.key())
                .sent(false)
                .paymentStatus("UNPAID")
                .deadline(value.deadline())
                .installment(value.installment())
                .userId(value.userId())
                .build();

        when(loanReminderRepository.save(loanReminderRetry)).thenReturn(any());

        // When
        underTest.save(record);

        // Then
        assertAll(() -> {
            verify(loanReminderRepository, times(1)).save(any());
        });
    }

}