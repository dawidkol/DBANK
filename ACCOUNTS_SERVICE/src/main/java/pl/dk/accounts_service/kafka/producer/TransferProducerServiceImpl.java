package pl.dk.accounts_service.kafka.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.dk.accounts_service.enums.TransferStatus;
import pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent;
import pl.dk.accounts_service.kafka.producer.dtos.ResponseTransferEvent;

import static pl.dk.accounts_service.kafka.KafkaConstants.*;

@Service
@RequiredArgsConstructor
class TransferProducerServiceImpl implements TransferProducerService {

    private final KafkaTemplate<String, ResponseTransferEvent> kafkaTemplate;

    @Override
    public void processTransfer(TransferEvent transferEvent, TransferStatus transferStatus) {
        ResponseTransferEvent producerResponse = buildResponse(transferEvent, transferStatus);
        kafkaTemplate.send(PROCESS_TRANSFER_EVENT, transferEvent.transferId(), producerResponse);
    }

    private ResponseTransferEvent buildResponse(TransferEvent transferEvent, TransferStatus transferStatus) {
        return ResponseTransferEvent.builder()
                .transferId(transferEvent.transferId())
                .transferStatus(transferStatus.name())
                .build();
    }
}
