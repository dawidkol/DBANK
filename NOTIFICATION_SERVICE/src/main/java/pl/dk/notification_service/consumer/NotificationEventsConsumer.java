package pl.dk.notification_service.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.dk.notification_service.consumer.dtos.UserDto;
import pl.dk.notification_service.notification.NotificationService;

import static pl.dk.notification_service.kafka.KafkaConstants.TOPIC_REGISTRATION;

@Component
@RequiredArgsConstructor
@Slf4j
class NotificationEventsConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = {TOPIC_REGISTRATION}, properties = "spring.json.value.default.type=pl.dk.notification_service.consumer.dtos.UserDto")
    public void onMessage(ConsumerRecord<String, UserDto> consumerRecord) {
        UserDto userDto = consumerRecord.value();
        log.info("ConsumerRecord: {}", consumerRecord);
        notificationService.sendUserEmailRegistrationConfirmation(userDto);
    }

}
