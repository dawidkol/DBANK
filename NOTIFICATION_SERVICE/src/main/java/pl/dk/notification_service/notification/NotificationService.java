package pl.dk.notification_service.notification;


import pl.dk.notification_service.notification.dtos.Email;
import pl.dk.notification_service.notification.dtos.Sms;

public interface NotificationService {

    void sendEmail(Email email);

    void sendSms(Sms sms);

}
