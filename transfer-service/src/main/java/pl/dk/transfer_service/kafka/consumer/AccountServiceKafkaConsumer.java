package pl.dk.transfer_service.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.transfer_service.kafka.KafkaConstants;
import pl.dk.transfer_service.kafka.consumer.dtos.ResponseTransferEvent;
import pl.dk.transfer_service.transfer.TransferService;
import pl.dk.transfer_service.enums.TransferStatus;

@Component
//@RequiredArgsConstructor
@Slf4j
class AccountServiceKafkaConsumer {

    private final TransferService transferService;

    public AccountServiceKafkaConsumer(TransferService transferService) {
        this.transferService = transferService;
    }

    @KafkaListener(topics = {KafkaConstants.PROCESS_TRANSFER_EVENT},
            properties = "spring.json.value.default.type=pl.dk.transfer_service.kafka.consumer.dtos.ResponseTransferEvent")
    @Transactional
    public void onMessage(ConsumerRecord<String, ResponseTransferEvent> consumerRecord) {
        ResponseTransferEvent transferEvent = consumerRecord.value();
        String transferId = transferEvent.transferId();
        String transferStatus = transferEvent.transferStatus();
        transferService.updateTransferStatus(transferId, TransferStatus.valueOf(transferStatus));
    }

}
