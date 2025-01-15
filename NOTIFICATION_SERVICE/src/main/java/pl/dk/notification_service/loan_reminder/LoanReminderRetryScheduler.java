package pl.dk.notification_service.loan_reminder;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
class LoanReminderRetryScheduler {

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
