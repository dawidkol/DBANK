package pl.dk.loanservice.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.kafka.consumer.dtos.CreatedLoanAccountEvent;
import pl.dk.loanservice.loan.Loan;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.loanDetails.LoanDetails;
import pl.dk.loanservice.loanDetails.LoanDetailsRepository;
import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleEvent;

import java.util.List;

import static pl.dk.loanservice.kafka.KafkaConstants.LOAN_ACCOUNT_CREATED;

@Component
@RequiredArgsConstructor
@Slf4j
class AccountServiceConsumer {

    private final LoanRepository loanRepository;
    private final LoanDetailsRepository loanDetailsRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @KafkaListener(topics = LOAN_ACCOUNT_CREATED,
            properties = "spring.json.value.default.type=pl.dk.loanservice.kafka.consumer.dtos.CreatedLoanAccountEvent")
    @Transactional
    public void consumeCreatedEventAccount(CreatedLoanAccountEvent createdLoanAccountEvent) {
        String loanId = createdLoanAccountEvent.loanId();
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new LoanNotExistsException("Loan with id: %s not exists".formatted(loanId)));
        buildAndSaveLoanDetailObject(createdLoanAccountEvent, loan);
        buildAndPublishLoanScheduleEvent(loan);
    }

    private void buildAndSaveLoanDetailObject(CreatedLoanAccountEvent createdLoanAccountEvent, Loan loan) {
        LoanDetails loanDetailsToSave = LoanDetails.builder()
                .loanAccountNumber(createdLoanAccountEvent.accountNumber())
                .loan(loan)
                .scheduleAvailable(false)
                .build();
        loanDetailsRepository.save(loanDetailsToSave);
    }

    private void buildAndPublishLoanScheduleEvent(Loan loan) {
        LoanScheduleEvent loanScheduleEvent = LoanScheduleEvent.builder()
                .amount(loan.getAmount())
                .interestRate(loan.getInterestRate())
                .loanId(loan.getId())
                .startDate(loan.getStartDate())
                .endDate(loan.getEndDate())
                .numberOfInstallments(loan.getNumberOfInstallments())
                .build();
        applicationEventPublisher.publishEvent(loanScheduleEvent);
    }
}
