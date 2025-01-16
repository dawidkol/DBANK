package pl.dk.notification_service.failed_message;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(properties =
        {"eureka.client.enabled=false",
                "scheduler.reminder.retry=0/1 * * * * *",
                "scheduler.reminder.clean=0/1 * * * * *"},
        webEnvironment = RANDOM_PORT)
class FailedMessageSchedulerTest {

    @MockitoSpyBean
    private FailedMessageScheduler underTest;

    @Test
    @DisplayName("It should invoke all scheduler methods successfully")
    void itShouldInvokeAllSchedulerMethodsSuccessfully() {
        // Given When Then
        await().atMost(1, SECONDS)
                .untilAsserted(() -> {
                    verify(underTest, atLeastOnce()).retryFailedLoanReminders();
                    verify(underTest, atLeastOnce()).cleanDatabase();
                });
    }
}