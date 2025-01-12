package pl.dk.loanservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.enums.PaymentStatus;
import pl.dk.loanservice.enums.TransferStatus;
import pl.dk.loanservice.kafka.consumer.dtos.LoanTransferDto;
import pl.dk.loanservice.loan_schedule.LoanSchedule;
import pl.dk.loanservice.loan_schedule.LoanScheduleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static pl.dk.loanservice.kafka.KafkaConstants.TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS;

@Slf4j
@Component
@RequiredArgsConstructor
class TransferServiceConsumer {

    private final LoanScheduleRepository loanScheduleRepository;

    @KafkaListener(topics = TRANSFER_SERVICE_UPDATE_LOAN_PAYMENT_STATUS,
            properties = "spring.json.value.default.type=pl.dk.loanservice.kafka.consumer.dtos.LoanTransferDto")
    @Transactional
    void consumeLoanService(ConsumerRecord<String, LoanTransferDto> record) {
        LoanTransferDto value = record.value();
        String transferId = value.id();
        LocalDateTime transferDate = value.transferDate();
        TransferStatus transferStatus = value.transferStatus();
        loanScheduleRepository.findByTransferId(transferId)
                .ifPresentOrElse(loanSchedule -> {
                    setPaymentStatus(loanSchedule, transferDate, transferStatus);
                }, () -> {
                    log.info("Nothing to update");
                });
    }

    private void setPaymentStatus(LoanSchedule loanSchedule, LocalDateTime transferDate, TransferStatus transferStatus) {
        LocalDate transferDateToLocalDate = transferDate.toLocalDate();
        if (transferStatus.equals(TransferStatus.COMPLETED)) {
            loanSchedule.setPaymentDate(transferDateToLocalDate);
            if (transferDateToLocalDate.isAfter(loanSchedule.getPaymentDate())) {
                loanSchedule.setPaymentStatus(PaymentStatus.PAID_LATE);
                loanSchedule.setPaymentDate(transferDateToLocalDate);
            } else {
                loanSchedule.setPaymentStatus(PaymentStatus.PAID_ON_TIME);
                loanSchedule.setPaymentDate(transferDateToLocalDate);
            }
        } else if (transferStatus.equals(TransferStatus.FAILED)) {
            if (transferDateToLocalDate.isAfter(loanSchedule.getPaymentDate())) {
                loanSchedule.setPaymentStatus(PaymentStatus.OVERDUE);
                loanSchedule.setPaymentDate(null);
            } else {
                loanSchedule.setPaymentStatus(PaymentStatus.UNPAID);
                loanSchedule.setPaymentDate(null);
            }
        } else if (transferStatus.equals(TransferStatus.CANCELLED)) {
            if (transferDateToLocalDate.isAfter(loanSchedule.getPaymentDate())) {
                loanSchedule.setPaymentStatus(PaymentStatus.OVERDUE);
                loanSchedule.setPaymentDate(null);
            } else {
                loanSchedule.setPaymentStatus(PaymentStatus.UNPAID);
                loanSchedule.setPaymentDate(null);
            }
        }
        String loanId = loanSchedule.getLoan().getId();
        PaymentStatus paymentStatus = loanSchedule.getPaymentStatus();
        log.info("Installment payment for a loan with id: {} is set to {}", loanId, paymentStatus);
    }
}

