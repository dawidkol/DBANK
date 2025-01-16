package pl.dk.notification_service.notification.dtos;

import lombok.Builder;

@Builder
public record Sms(String to,
                  String message) {
}
