package pl.dk.notification_service.notification.dtos;

import lombok.Builder;

@Builder
public record Email(String to,
                    String subject,
                    String message) {
}
