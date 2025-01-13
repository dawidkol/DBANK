package pl.dk.loanservice.loan_schedule;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static java.util.concurrent.TimeUnit.*;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false",
                "scheduler.payment-status.overdue=0/1 * * * * *",
                "scheduler.payment-status.pending=0/1 * * * * *",
                "scheduler.payment-status.loan-payment=0/1 * * * * *"})
class SchedulerLoanScheduleTest {

    @MockitoSpyBean
    private SchedulerLoanSchedule underTest;

    @Test
    @DisplayName("It should invoke all scheduled methods")
    void itShouldInvokeAllScheduledMethods() {
        // Given When Then
        await().atMost(1, SECONDS)
                .untilAsserted(() -> {
                    verify(underTest, atLeastOnce()).setPaymentStatusAsOverdue();
                    verify(underTest, atLeastOnce()).setScheduledLoanPaymentToPending();
                    verify(underTest, atLeastOnce()).updateLoanSchedulePayment();
                });
    }
}