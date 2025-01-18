package pl.dk.accounts_service.kafka.producer;

import pl.dk.accounts_service.enums.TransferStatus;
import pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent;

public interface TransferProducerService {

    void processTransfer(TransferEvent transferEvent, TransferStatus transferStatus);
}
