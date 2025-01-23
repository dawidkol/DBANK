package pl.dk.cardservice.httpclient.dto;

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