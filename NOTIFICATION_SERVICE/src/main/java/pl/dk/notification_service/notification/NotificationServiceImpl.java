package pl.dk.notification_service.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.dk.notification_service.consumer.dtos.UserDto;

@Service
@RequiredArgsConstructor
class NotificationServiceImpl implements NotificationService {

    @Value("${app.mail.username}")
    private String email;

    @Value("${twilio.account-sid}")
    private String ACCOUNT_SID;

    @Value("${twilio.auth-token}")
    private String AUTH_TOKEN;

    @Value("${twilio.phone.number.from}")
    private String phoneNumberFrom;

    private final JavaMailSender javaMailSender;

    @Override
    @Async
    public void sendUserEmailRegistrationConfirmation(UserDto userDto) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(email);
        simpleMailMessage.setSubject("Welcome to DBANK, %s We're Excited to Have You Onboard!".formatted(userDto.firstName()));
        simpleMailMessage.setTo(userDto.email());
        simpleMailMessage.setText(createRegistrationMessage(userDto));
        javaMailSender.send(simpleMailMessage);

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

    @Override
    public void sendUserSmsRegistrationConfirmation(UserDto userDto) {
        sendTwilioNotification(userDto.phone(), createRegistrationMessage(userDto));
    }

    @Async
    void sendTwilioNotification(String phoneNumberTo, String message) {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Message.creator(
                new PhoneNumber(phoneNumberTo),
                new PhoneNumber(phoneNumberFrom),
                (message)).create();
    }
}
