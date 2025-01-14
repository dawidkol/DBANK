package pl.dk.notification_service.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.dk.notification_service.kafka.consumer.dtos.LoanScheduleReminder;
import pl.dk.notification_service.kafka.consumer.dtos.UserDto;
import pl.dk.notification_service.httpClient.UserServiceFeignClient;
import pl.dk.notification_service.loan_reminder.LoanReminderRetryService;
import pl.dk.notification_service.notification.NotificationService;
import pl.dk.notification_service.notification.dtos.Email;

import static pl.dk.notification_service.kafka.KafkaConstants.*;

@Component
@Slf4j
class EmailNotification {

    private final NotificationService notificationService;
    private final UserServiceFeignClient userServiceFeignClient;
    private final String email;
    private final LoanReminderRetryService loanReminderRetryService;


    public EmailNotification(NotificationService notificationService,
                             UserServiceFeignClient userServiceFeignClient,
                             @Value("${app.mail.username}") String email1,
                             LoanReminderRetryService loanReminderRetryService) {
        this.notificationService = notificationService;
        this.userServiceFeignClient = userServiceFeignClient;
        this.email = email1;
        this.loanReminderRetryService = loanReminderRetryService;
    }

    @Async
    @KafkaListener(topics = {TOPIC_REGISTRATION},
            properties = "spring.json.value.default.type=pl.dk.notification_service.kafka.consumer.dtos.UserDto")
    public void onUserServiceMessage(ConsumerRecord<String, UserDto> consumerRecord) {
        UserDto userDto = consumerRecord.value();
        log.info("ConsumerRecord: {}", consumerRecord);
        Email email = Email.builder()
                .to(userDto.email())
                .subject(createRegistrationConfirmationSubject(userDto))
                .message(createRegistrationMessage(userDto))
                .build();
        notificationService.sendEmail(email);
    }

    private String createRegistrationConfirmationSubject(UserDto userDto) {
        return "Welcome to DBANK, %s We're Excited to Have You Onboard!"
                .formatted(userDto.firstName());
    }

    private String createRegistrationMessage(UserDto userDto) {
        return """
                Hello %s %s
                
                Welcome to DBANK! We're thrilled to have you on board.
                
                If you have any questions or need assistance, feel free to reach out to us at %s. We're here to help!
                
                Best regards,
                The DBANK Team
                """.formatted(userDto.firstName(), userDto.lastName().toCharArray()[0] + ".", email);
    }

    @Async
    @KafkaListener(topics = LOAN_SERVICE_LOAN_REMINDER,
            properties = "spring.json.value.default.type=pl.dk.notification_service.kafka.consumer.dtos.LoanScheduleReminder")
    public void onLoanServiceMessage(ConsumerRecord<String, LoanScheduleReminder> record, Acknowledgment acknowledgment) {
        LoanScheduleReminder loanScheduleReminder = record.value();
        String userId = loanScheduleReminder.userId();
        try {
            ResponseEntity<UserDto> userById = userServiceFeignClient.getUserById(userId);
            if (userById.getStatusCode().isSameCodeAs(HttpStatus.OK)) {
                UserDto userDto = userById.getBody();
                Email email = Email.builder()
                        .to(userDto.email())
                        .subject("Loan payment reminder")
                        .message(createLoanReminderMessage(userDto, loanScheduleReminder))
                        .build();
                notificationService.sendEmail(email);
                acknowledgment.acknowledge();
            } else {
                log.warn("User-Service returned status {} for userId {}", userById.getStatusCode(), userId);
                throw new RuntimeException("User-Service returned non-OK status");
            }
        } catch (RuntimeException ex) {
            log.error("Error processing loan reminder for userId {}: {}", userId, ex.getMessage());
            loanReminderRetryService.save(record);
        }
    }

    private String createLoanReminderMessage(UserDto userDto, LoanScheduleReminder loanScheduleReminder) {
        return """
                Hello %s,
                
                We hope this message finds you well. This is a friendly reminder that your loan payment deadline is approaching on %s.
                
                Here are the details:
                - Installment amount: %s
                
                Please ensure your payment is completed by the deadline to avoid any penalties.
                
                If you have any questions or concerns, feel free to reach out to our support team.
                
                Best regards,
                The DBANK Team
                """.formatted(
                userDto.firstName(),
                loanScheduleReminder.deadline(),
                loanScheduleReminder.installment());
    }

}
