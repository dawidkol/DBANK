package pl.dk.notification_service.failed_message;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dk.notification_service.failed_message.loan_schedule.LoanReminderRetryService;


@Component
@RequiredArgsConstructor
class FailedMessageScheduler {

    private final LoanReminderRetryService loanReminderRetryService;

    @Async
    @Scheduled(cron = "${scheduler.reminder.retry}")
    void retryFailedLoanReminders() {
        loanReminderRetryService.retryFailedLoanReminders();
    }

    @Async
    @Scheduled(cron = "${scheduler.reminder.clean}")
    void cleanDatabase() {
        loanReminderRetryService.cleanDatabase();
    }
}
