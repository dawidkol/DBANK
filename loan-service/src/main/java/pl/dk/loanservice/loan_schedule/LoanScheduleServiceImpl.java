package pl.dk.loanservice.loan_schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import pl.dk.loanservice.enums.PaymentStatus;
import pl.dk.loanservice.exception.LoanDetailsNotExistsException;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.loan.Loan;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.loan.LoanService;
import pl.dk.loanservice.loan_details.LoanDetailsRepository;
import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleDto;
import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleEvent;
import pl.dk.loanservice.loan_schedule.dtos.UpdateSchedulePaymentEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static pl.dk.loanservice.enums.PaymentStatus.*;

@Service
@RequiredArgsConstructor
@Slf4j
class LoanScheduleServiceImpl implements LoanScheduleService {

    private final LoanScheduleRepository loanScheduleRepository;
    private final LoanRepository loanRepository;
    private final LoanService loanService;
    private final LoanDetailsRepository loanDetailsRepository;

    @Override
    @ApplicationModuleListener
    public void createSchedule(LoanScheduleEvent loanScheduleEvent) {
        LocalDate startDate = loanScheduleEvent.startDate();
        Integer numberOfInstallments = loanScheduleEvent.numberOfInstallments();
        BigDecimal installment = loanService.calculateMonthlyInstallment(
                loanScheduleEvent.amount(),
                loanScheduleEvent.interestRate(),
                numberOfInstallments
        );
        String loanId = loanScheduleEvent.loanId();
        Loan loan = loanRepository.findById(loanId).orElseThrow(
                () -> new LoanNotExistsException("Loan with id: %s not exists".formatted(loanId)));
        List<LoanSchedule> loanSchedules = new ArrayList<>();
        for (int i = 0; i < numberOfInstallments; i++) {
            LocalDate localDate = startDate.plusMonths(i);
            LocalDate deadline = LocalDate.of(localDate.getYear(), localDate.getMonth(), localDate.lengthOfMonth());
            LoanSchedule loanScheduleToSave = LoanSchedule.builder()
                    .installment(installment)
                    .deadline(deadline)
                    .paymentStatus(UNPAID)
                    .loan(loan)
                    .build();
            loanSchedules.add(loanScheduleToSave);
        }
        loanScheduleRepository.saveAll(loanSchedules);
        updateLoanDetailsRecord(loanId);
    }

    private void updateLoanDetailsRecord(String loanId) {
        loanDetailsRepository.findByLoan_id(loanId).ifPresentOrElse(details -> {
            details.setScheduleAvailable(true);
        }, () -> {
            throw new LoanDetailsNotExistsException("Loan detail with loanId: %s not exists".formatted(loanId));
        });
    }

    @Override
    @ApplicationModuleListener
    public void updatePaymentInstallmentStatus(UpdateSchedulePaymentEvent event) {
        String loanScheduleId = event.loanScheduleId();
        LocalDate transferLocalDate = event.transferDate().toLocalDate();
        LocalDate now = LocalDate.now();
        loanScheduleRepository.findById(loanScheduleId)
                .ifPresentOrElse(loanSchedule -> {
                    if (now.isEqual(transferLocalDate) || now.isAfter(transferLocalDate)) {
                        setLoanSchedulePaymentStatusAndTransferId(event, loanSchedule, PENDING);
                    } else if (now.isBefore(transferLocalDate)) {
                        setLoanSchedulePaymentStatusAndTransferId(event, loanSchedule, SCHEDULED);
                    }
                }, () -> {
                    log.info("LoanSchedule: Nothing to update");
                });
    }

    private static void setLoanSchedulePaymentStatusAndTransferId(UpdateSchedulePaymentEvent event,
                                                                  LoanSchedule loanSchedule,
                                                                  PaymentStatus paymentStatus) {
        loanSchedule.setPaymentStatus(paymentStatus);
        loanSchedule.setTransferId(event.transferId());
        loanSchedule.setPaymentDate(event.transferDate().toLocalDate());
    }

    @Override
    public List<LoanScheduleDto> getLoanSchedule(String loan_id) {
        return loanScheduleRepository.findAllByLoan_id(loan_id)
                .stream()
                .map(LoanScheduleDtoMapper::map)
                .toList();
    }
}
