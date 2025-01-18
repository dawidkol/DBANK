package pl.dk.loanservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import pl.dk.loanservice.loan_schedule.dtos.TransferId;

import static pl.dk.loanservice.kafka.KafkaConstants.LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS;

@Component
@RequiredArgsConstructor
@Slf4j
class LoanScheduleProducer {

    private final KafkaTemplate<String, TransferId> loanScheduleTransferUpdate;

    @ApplicationModuleListener
    void consume(TransferId transferId) {
        log.warn("Starting sending events to {}", LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS);
        loanScheduleTransferUpdate.send(
                LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS,
                transferId.transferId(),
                transferId);
        log.warn("Sending events to {} ended", LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS);
    }
}
