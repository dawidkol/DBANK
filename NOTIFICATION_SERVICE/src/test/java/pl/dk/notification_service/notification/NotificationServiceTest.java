package pl.dk.notification_service.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import pl.dk.notification_service.notification.dtos.Email;
import pl.dk.notification_service.notification.dtos.Sms;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class NotificationServiceTest {

    private NotificationService underTest;

    @BeforeEach
    void setUp() {
        JavaMailSender javaMailSender = new JavaMailSenderForTest();
        String email = "test.email@test.pl";
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        String twilioPhoneNumber = System.getenv("TWILIO_PHONE_NUMBER");
        underTest = new NotificationServiceImpl(email, accountSid, authToken, twilioPhoneNumber, javaMailSender);
    }

    @Test
    @DisplayName("It should sent email successfully")
    void itShouldSentEmailSuccessfully() {
        // Given
        Email testMessage = Email.builder()
                .to("john.doe@test.pl")
                .subject("Hello from test class!!")
                .message("Test message")
                .build();

        // When // Then
        assertDoesNotThrow(() -> underTest.sendEmail(testMessage));
    }

    @Test
    @DisplayName("It should sent sms successfully")
     void itShouldSentSmsSuccessfully() {
        // Given
        Sms sms = Sms.builder()
                .to(System.getenv("MY_PHONE_NUMBER"))
                .message("Hello from test class")
                .build();

        // When Then
        assertDoesNotThrow(() -> underTest.sendSms(sms));
    }
}