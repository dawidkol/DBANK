package pl.dk.notification_service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.dk.notification_service.notification.NotificationService;

@Component
@RequiredArgsConstructor
class SmsNotification {

    private final NotificationService notificationService;
}
