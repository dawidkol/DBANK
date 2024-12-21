package pl.dk.accounts_service.kafka.producer;

import pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent;

public interface TransferProducerService {

    void processTransferSuccessfully(TransferEvent transferEvent);
    void processTransferFailure(TransferEvent transferEvent);
}
