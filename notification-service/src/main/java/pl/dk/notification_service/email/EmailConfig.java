package pl.dk.notification_service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


import java.util.Properties;

@Configuration
class EmailConfig {

    private final String host;
    private final int port;
    private final String email;
    private final String password;
    private final String protocolKey;
    private final String protocolValue;
    private final String smtpKey;
    private final String smtpValue;
    private final String starttlsKey;
    private final String starttlsValue;

    public EmailConfig(@Value("${app.mail.host}") String host,
                       @Value("${app.mail.port}") int port,
                       @Value("${app.mail.username}") String email,
                       @Value("${app.mail.password}") String password,
                       @Value("${app.mail.protocol-key}") String protocolKey,
                       @Value("${app.mail.protocol-value}") String protocolValue,
                       @Value("${app.mail.smtp-key}") String smtpKey,
                       @Value("${app.mail.smtp-value}") String smtpValue,
                       @Value("${app.mail.starttls-key}") String starttlsKey,
                       @Value("${app.mail.starttls-value}") String starttlsValue) {
        this.host = host;
        this.port = port;
        this.email = email;
        this.password = password;
        this.protocolKey = protocolKey;
        this.protocolValue = protocolValue;
        this.smtpKey = smtpKey;
        this.smtpValue = smtpValue;
        this.starttlsKey = starttlsKey;
        this.starttlsValue = starttlsValue;
    }

    @Bean
    public JavaMailSender createJavaMailSenderImpl() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setPort(port);
        javaMailSender.setHost(host);
        javaMailSender.setUsername(email);
        javaMailSender.setPassword(password);
        Properties props = javaMailSender.getJavaMailProperties();
        props.put(protocolKey, protocolValue);
        props.put(smtpKey, smtpValue);
        props.put(starttlsKey, starttlsValue);
        return javaMailSender;
    }
}
