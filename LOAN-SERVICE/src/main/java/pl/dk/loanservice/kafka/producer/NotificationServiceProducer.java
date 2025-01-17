package pl.dk.loanservice.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.dk.loanservice.enums.PaymentStatus;
import pl.dk.loanservice.kafka.producer.dtos.LoanScheduleReminder;
import pl.dk.loanservice.loan_schedule.LoanSchedule;
import pl.dk.loanservice.loan_schedule.LoanScheduleRepository;

import java.time.LocalDate;
import java.util.List;

import static pl.dk.loanservice.enums.PaymentStatus.UNPAID;
import static pl.dk.loanservice.kafka.KafkaConstants.LOAN_SERVICE_LOAN_REMINDER;

@Component
@Slf4j
@RequiredArgsConstructor
class NotificationServiceProducer {

    private final KafkaTemplate<String, LoanScheduleReminder> loanScheduleReminderKafkaTemplate;
    private final LoanScheduleRepository loanScheduleRepository;

    @Async
    @Scheduled(cron = "${scheduler.payment-status.loan-schedule-reminder}")
    public void produceReminderMessage() {
        LocalDate localDate = LocalDate.now().minusDays(3);
        List<PaymentStatus> paymentStatuses = List.of(UNPAID);
        loanScheduleRepository.findAllByDeadlineBeforeAndPaymentStatusIn(
                        localDate, paymentStatuses)
                .forEach(loanSchedule -> {
                    LoanScheduleReminder loanScheduleReminder = LoanScheduleReminder.builder()
                            .loanScheduleId(loanSchedule.getId())
                            .deadline(loanSchedule.getDeadline())
                            .paymentStatus(loanSchedule.getPaymentStatus().name())
                            .installment(loanSchedule.getInstallment())
                            .build();
                    loanScheduleReminderKafkaTemplate.send(
                            LOAN_SERVICE_LOAN_REMINDER,
                            loanScheduleReminder.loanScheduleId(),
                            loanScheduleReminder);
                });
    }

}
