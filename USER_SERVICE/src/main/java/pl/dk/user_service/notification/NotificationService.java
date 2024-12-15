package pl.dk.user_service.notification;

import pl.dk.user_service.user.dto.UserDto;

public interface NotificationService {

    void sendToRegistrationTopic(UserDto result);

}
