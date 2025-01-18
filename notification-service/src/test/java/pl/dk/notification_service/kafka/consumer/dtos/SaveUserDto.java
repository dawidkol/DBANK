package pl.dk.notification_service.kafka.consumer;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SaveUserDto(

        String firstName,
        String lastName,
        String email,
        String phone,
        String password,
        LocalDate dateOfBirth

) {

}
