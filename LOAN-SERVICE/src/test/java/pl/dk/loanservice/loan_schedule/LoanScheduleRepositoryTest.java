package pl.dk.loanservice.loan_schedule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import pl.dk.loanservice.enums.CurrencyType;
import pl.dk.loanservice.enums.PaymentStatus;
import pl.dk.loanservice.loan.Loan;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class LoanScheduleRepositoryTest {

    @Autowired
    private LoanScheduleRepository underTest;

    @Autowired
    private LoanRepository loanRepository;

    private List<LoanSchedule> loanSchedules;

    @BeforeEach
    void setUp() {
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        BigDecimal amount = BigDecimal.valueOf(5000);
        LocalDate startDate = LocalDate.now().plusMonths(1);
        LocalDate endDate = startDate.plusMonths(12);
        String description = "Loan for purchasing new office equipment";
        BigDecimal interestRate = BigDecimal.valueOf(4.5);
        LoanStatus status = LoanStatus.PENDING;
        BigDecimal remainingAmount = amount;
        Integer numberOfInstallments = 24;

        Loan loan = Loan.builder()
                .userId(userId)
                .amount(amount)
                .interestRate(interestRate)
                .startDate(startDate)
                .endDate(endDate)
                .remainingAmount(remainingAmount)
                .status(status)
                .description(description)
                .numberOfInstallments(numberOfInstallments)
                .currencyType(CurrencyType.PLN)
                .build();

        loanRepository.save(loan);
        loanRepository.flush();

        LocalDate deadline = LocalDate.now().minusDays(1);
        LoanSchedule loanSchedule1 = LoanSchedule.builder()
                .installment(new BigDecimal("500.00"))
                .deadline(deadline)
                .paymentStatus(PaymentStatus.UNPAID)
                .loan(loan)
                .build();
        LoanSchedule loanSchedule2 = LoanSchedule.builder()
                .installment(new BigDecimal("450.00"))
                .deadline(deadline)
                .paymentStatus(PaymentStatus.UNPAID)
                .loan(loan)
                .build();
        LoanSchedule loanSchedule3 = LoanSchedule.builder()
                .installment(new BigDecimal("600.00"))
                .deadline(deadline)
                .paymentStatus(PaymentStatus.UNPAID)
                .loan(loan)
                .build();
        LoanSchedule loanSchedule4 = LoanSchedule.builder()
                .installment(new BigDecimal("550.00"))
                .deadline(deadline)
                .paymentStatus(PaymentStatus.UNPAID)
                .loan(loan)
                .build();
        LoanSchedule loanSchedule5 = LoanSchedule.builder()
                .installment(new BigDecimal("700.00"))
                .deadline(deadline)
                .paymentStatus(PaymentStatus.UNPAID)
                .loan(loan)
                .build();
        loanSchedules = List.of(loanSchedule1, loanSchedule2, loanSchedule3, loanSchedule4, loanSchedule5);
        underTest.saveAll(loanSchedules);
    }

    @Test
    @DisplayName("It should set all rows as Overdue ")
    void itShouldSetAllRowsAsOverdue() {
        // Given

        // When
        int rowsAffected = underTest.setPaymentStatusFromUnpaidTo(PaymentStatus.OVERDUE);

        // Then
        assertAll(() -> {
            assertEquals(loanSchedules.size(), rowsAffected);
        });
    }
}