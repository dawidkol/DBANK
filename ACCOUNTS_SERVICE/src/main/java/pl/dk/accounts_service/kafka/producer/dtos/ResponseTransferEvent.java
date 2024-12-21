package pl.dk.accounts_service.kafka.producer.dtos;

import lombok.Builder;


@Builder
public record ResponseTransferEvent(
        String transferId,
        String transferStatus
) {
}
