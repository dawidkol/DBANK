package pl.dk.loanservice.loan_schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.loan_schedule.dtos.TransferId;

import java.util.List;

import static pl.dk.loanservice.enums.PaymentStatus.*;
import static pl.dk.loanservice.kafka.KafkaConstants.LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS;

@Component
@RequiredArgsConstructor
@Slf4j
class SchedulerLoanSchedule {

    private final LoanScheduleRepository loanScheduleRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Async
    @Scheduled(cron = "${scheduler.payment-status.overdue}")
    public void setPaymentStatusAsOverdue() {
        log.info("Starting updating LoanSchedule records");
        int updatedRows = loanScheduleRepository.setPaymentStatusFromUnpaidTo(OVERDUE);
        log.info("Updated LoanSchedule rows {}", updatedRows);
    }

    @Async
    @Scheduled(cron = "${scheduler.payment-status.pending}")
    public void setScheduledLoanPaymentToPending() {
        int rowsAffected = loanScheduleRepository.updateStatusFromScheduledTo(PENDING);
        log.info("Set {} rows payment_status to {}}", rowsAffected, PENDING);
    }

    @Async
    @Scheduled(cron = "${scheduler.payment-status.loan-payment}")
    @Transactional
    public void updateLoanSchedulePayment() {
        List<LoanSchedule> list = loanScheduleRepository.findAllByPaymentStatus(PENDING)
                .stream()
                .peek(lS -> {
                    lS.setPaymentStatus(WAITING_FOR_CONFIRMATION);
                    applicationEventPublisher.publishEvent(new TransferId(lS.getTransferId()));
                })
                .toList();

        log.info("Total ids to send to kafka topic({}) = {}", LOAN_SERVICE_UPDATE_LOAN_PAYMENT_STATUS, list.size());
    }

}
