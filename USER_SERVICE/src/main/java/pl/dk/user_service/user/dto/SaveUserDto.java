package pl.dk.user_service.user.dto;

import java.time.LocalDate;

import lombok.Builder;

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
