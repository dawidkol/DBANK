package pl.dk.loanservice.httpClient.dtos;

import lombok.Builder;

@Builder
public record UserDto(
        String userId,
        String firstName,
        String lastName,
        String phone,
        String email) {
}