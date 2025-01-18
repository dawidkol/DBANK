package pl.dk.user_service.user.dto;

import lombok.Builder;

@Builder
public record UserDto(
        String userId,
        String firstName,
        String lastName,
        String phone,
        String email,
        Boolean active) {
}