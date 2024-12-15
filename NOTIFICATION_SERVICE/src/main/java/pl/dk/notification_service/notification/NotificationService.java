package pl.dk.notification_service.notification;


import pl.dk.notification_service.consumer.dtos.UserDto;

public interface NotificationService {

    void sendUserEmailRegistrationConfirmation(UserDto userDto);
    void sendUserSmsRegistrationConfirmation(UserDto userDto);
}
