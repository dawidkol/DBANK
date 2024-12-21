package pl.dk.transfer_service.kafka.consumer.dtos;

import lombok.Builder;


@Builder
public record ResponseTransferEvent(
        String transferId,
        String transferStatus
) {
}
