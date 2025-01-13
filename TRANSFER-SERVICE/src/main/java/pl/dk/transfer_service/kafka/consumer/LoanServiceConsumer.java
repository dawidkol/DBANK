package pl.dk.transfer_service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.dk.transfer_service.kafka.consumer.dtos.TransferId;
import pl.dk.transfer_service.transfer.TransferRepository;
import pl.dk.transfer_service.kafka.consumer.dtos.LoanTransferDto;

import static pl.dk.transfer_service.kafka.KafkaConstants.LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS;
import static pl.dk.transfer_service.kafka.KafkaConstants.TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS;

@Component
@RequiredArgsConstructor
@Slf4j
class LoanServiceConsumer {

    private final KafkaTemplate<String, LoanTransferDto> loanTransferDtoKafkaTemplate;
    private final TransferRepository transferRepository;

    @Async
    @KafkaListener(topics = LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS,
            properties = "spring.json.value.default.type=pl.dk.transfer_service.kafka.consumer.dtos.TransferId")
    public void sendUpdateLoanPaymentStatusToKafka(ConsumerRecord<String, TransferId> record) {
        String transferId = record.value().transferId();
        transferRepository.findById(transferId)
                .ifPresentOrElse(transfer -> {
                    loanTransferDtoKafkaTemplate.send(
                            TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS,
                            transferId,
                            new LoanTransferDto(transferId, transfer.getTransferDate(), transfer.getTransferStatus()));
                }, () -> {
                    log.warn("Transfer with id {} not exists", transferId);
                });

    }
}
