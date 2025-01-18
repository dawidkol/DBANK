package pl.dk.user_service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import pl.dk.user_service.notification.NotificationService;
import pl.dk.user_service.user.dto.UserDto;

import java.util.concurrent.CompletableFuture;

import static pl.dk.user_service.kafka.KafkaConstants.TOPIC_REGISTRATION;

@Service
@AllArgsConstructor
@Slf4j
class NotificationServiceKafkaImpl implements NotificationService {

    private final KafkaTemplate<String, UserDto> kafkaTemplate;

    @Override
    public void sendToRegistrationTopic(UserDto result) {
        CompletableFuture<SendResult<String, UserDto>> sendResultCompletableFuture = kafkaTemplate.send(TOPIC_REGISTRATION, result.userId(), result)
                .whenComplete((sendResult, throwable) -> {
                    if (throwable != null) {
                        handleFailure(throwable);
                    } else {
                        handleSuccess(result, sendResult);
                    }
                });
    }

    private void handleSuccess(UserDto result, SendResult<String, UserDto> sendResult) {
        log.info("Message sent successfully for the key: {} and the value: {}, partition is {}",
                result.userId(), result, sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(Throwable throwable) {
        log.error("Error sending the message and the exception is {} ", throwable.getMessage());
    }

}

