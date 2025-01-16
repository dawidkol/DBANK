package pl.dk.notification_service.notification;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pl.dk.notification_service.notification.dtos.Email;
import pl.dk.notification_service.notification.dtos.Sms;

@Service
class NotificationServiceImpl implements NotificationService {

    private final String email;
    private final String accountSid;
    private final String authToken;
    private final String twilioPhoneNumber;
    private final JavaMailSender javaMailSender;

    public NotificationServiceImpl(
            @Value("${app.mail.username}") String email,
            @Value("${twilio.account-sid}") String accountSid,
            @Value("${twilio.auth-token}") String authToken,
            @Value("${twilio.phone-number}") String twilioPhoneNumber,
            JavaMailSender javaMailSender) {
        this.email = email;
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.twilioPhoneNumber = twilioPhoneNumber;
        this.javaMailSender = javaMailSender;
    }

    @Override
    @Async
    public void sendEmail(Email email) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom(this.email);
        simpleMailMessage.setSubject(email.subject());
        simpleMailMessage.setTo(email.to());
        simpleMailMessage.setText(email.message());
        javaMailSender.send(simpleMailMessage);
    }

    @Override
    @Async
    public void sendSms(Sms sms) {
        Twilio.init(accountSid, authToken);
        Message.creator(new PhoneNumber(sms.to()),
                        new PhoneNumber(twilioPhoneNumber),
                        (sms.message()))
                .create();
    }

}
