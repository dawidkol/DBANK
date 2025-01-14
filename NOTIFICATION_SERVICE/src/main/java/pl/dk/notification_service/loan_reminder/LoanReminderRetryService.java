package pl.dk.notification_service.loan_reminder;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import pl.dk.notification_service.kafka.consumer.dtos.LoanScheduleReminder;

public interface LoanReminderRetryService {

    void save(ConsumerRecord<String, LoanScheduleReminder> record);

    void retryFailedLoanReminders();

    void cleanDatabase();
}
