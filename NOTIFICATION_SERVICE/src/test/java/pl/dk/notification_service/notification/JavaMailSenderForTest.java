package pl.dk.notification_service.notification;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.InputStream;

@Slf4j
class JavaMailSenderForTest implements JavaMailSender {
    @Override
    public MimeMessage createMimeMessage() {
        log.info("Message sent");
        return null;
    }

    @Override
    public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
        log.info("Message sent");
        return null;
    }

    @Override
    public void send(MimeMessage... mimeMessages) throws MailException {
        log.info("Message sent");
    }

    @Override
    public void send(SimpleMailMessage... simpleMessages) throws MailException {
        log.info("Message sent");
    }
}
