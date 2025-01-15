package pl.dk.notification_service.loan_reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.notification_service.kafka.consumer.dtos.LoanScheduleReminder;

import java.time.LocalDate;
import java.util.List;

import static pl.dk.notification_service.kafka.KafkaConstants.LOAN_SERVICE_LOAN_REMINDER;

@Service
@RequiredArgsConstructor
@Slf4j
class LoanReminderRetryServiceImpl implements LoanReminderRetryService {

    private final LoanReminderRepository loanReminderRepository;
    private final KafkaTemplate<String, LoanScheduleReminder> loanScheduleKafkaTemplate;

    @Override
    @Transactional
    public void save(ConsumerRecord<String, LoanScheduleReminder> record) {
        LoanScheduleReminder value = record.value();
        LoanReminderRetry toSave = LoanReminderRetry.builder()
                .id(record.key())
                .installment(value.installment())
                .deadline(value.deadline())
                .paymentStatus(value.paymentStatus())
                .userId(value.userId())
                .sent(false)
                .build();
        loanReminderRepository.save(toSave);
    }

    @Override
    @Transactional
    public void retryFailedLoanReminders() {
        LocalDate now = LocalDate.now();
        List<LoanReminderRetry> list = loanReminderRepository.findAllByDeadlineIsLessThanEqualAndSent(
                        now,
                        false,
                        PageRequest.of(0, 1000))
                .stream()
                .peek(loanReminderRetry -> {
                    LoanScheduleReminder loanScheduleReminderData = LoanScheduleReminder.builder()
                            .id(loanReminderRetry.getId())
                            .deadline(loanReminderRetry.getDeadline())
                            .installment(loanReminderRetry.getInstallment())
                            .paymentStatus(loanReminderRetry.getPaymentStatus())
                            .build();
                    loanScheduleKafkaTemplate.send(
                            LOAN_SERVICE_LOAN_REMINDER,
                            loanReminderRetry.getId(),
                            loanScheduleReminderData);
                    loanReminderRetry.setSent(true);
                }).toList();
        if (!list.isEmpty()) {
            loanReminderRepository.saveAll(list);
        }
    }

    @Override
    @Transactional
    public void cleanDatabase() {
        log.info("Starting deleting LoanReminderRetry records fro db");
        LocalDate beforeSixMonths = LocalDate.now().minusMonths(6);
        Page<LoanReminderRetry> allByDeadlineIsBefore = loanReminderRepository.findAllByDeadlineIsBefore(
                beforeSixMonths,
                PageRequest.of(0, 1000));
        loanReminderRepository.deleteAll(allByDeadlineIsBefore);
        log.info("{} rows deleted from db", allByDeadlineIsBefore.getTotalElements());
    }
}
