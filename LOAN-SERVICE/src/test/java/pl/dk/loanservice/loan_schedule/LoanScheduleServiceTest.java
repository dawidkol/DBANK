package pl.dk.loanservice.loan_schedule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.dk.loanservice.exception.LoanDetailsNotExistsException;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.loan.Loan;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.loan.LoanService;
import pl.dk.loanservice.loan_details.LoanDetails;
import pl.dk.loanservice.loan_details.LoanDetailsRepository;
import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleDto;
import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleEvent;
import pl.dk.loanservice.loan_schedule.dtos.UpdateSchedulePaymentEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;
import static pl.dk.loanservice.loan_schedule.PaymentStatus.*;

class LoanScheduleServiceTest {

    @Mock
    private LoanScheduleRepository loanScheduleRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanService loanService;

    @Mock
    private LoanDetailsRepository loanDetailsRepository;

    private LoanScheduleService underTest;

    private LoanScheduleEvent loanScheduleEvent;

    private UpdateSchedulePaymentEvent updateSchedulePaymentEvent;

    private AutoCloseable autoCloseable;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new LoanScheduleServiceImpl(loanScheduleRepository,
                loanRepository,
                loanService,
                loanDetailsRepository);

        LocalDate startDate = LocalDate.now().plusMonths(1);
        LocalDate endDate = startDate.plusYears(3);
        int numberOfInstallments = (int) Period.between(startDate, endDate).toTotalMonths();
        String loanId = UUID.randomUUID().toString();
        loanScheduleEvent = LoanScheduleEvent.builder()
                .loanId(loanId)
                .amount(BigDecimal.valueOf(1000000))
                .startDate(startDate)
                .endDate(endDate)
                .interestRate(BigDecimal.valueOf(8))
                .numberOfInstallments(numberOfInstallments)
                .build();

        updateSchedulePaymentEvent = UpdateSchedulePaymentEvent.builder()
                .loanId(loanId)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should create schedule successfully")
    void itShouldCreateScheduleSuccessfully() {
        // Given
        when(loanService.calculateMonthlyInstallment(loanScheduleEvent.amount(),
                loanScheduleEvent.interestRate(),
                loanScheduleEvent.numberOfInstallments()))
                .thenReturn(BigDecimal.valueOf(300));
        when(loanRepository.findById(loanScheduleEvent.loanId()))
                .thenReturn(Optional.of(Loan.builder()
                        .id(loanScheduleEvent.loanId())
                        .build()));
        when(loanDetailsRepository.findByLoan_id(loanScheduleEvent.loanId()))
                .thenReturn(Optional.of(LoanDetails.builder()
                        .scheduleAvailable(false)
                        .build()));
        // When
        underTest.createSchedule(loanScheduleEvent);

        // Then
        assertAll(() -> {
            verify(loanService, times(1)).calculateMonthlyInstallment(
                    loanScheduleEvent.amount(),
                    loanScheduleEvent.interestRate(),
                    loanScheduleEvent.numberOfInstallments());
            verify(loanRepository, times(1))
                    .findById(loanScheduleEvent.loanId());
            verify(loanDetailsRepository, times(1))
                    .findByLoan_id(loanScheduleEvent.loanId());
            verify(loanScheduleRepository, times(1))
                    .saveAll(anyCollection());
        });
    }

    @Test
    @DisplayName("It should throw LoanNotExistsException")
    void itShouldThrowLoanNotExistsException() {
        // Given
        when(loanService.calculateMonthlyInstallment(loanScheduleEvent.amount(),
                loanScheduleEvent.interestRate(),
                loanScheduleEvent.numberOfInstallments()))
                .thenReturn(BigDecimal.valueOf(300));
        when(loanRepository.findById(loanScheduleEvent.loanId()))
                .thenReturn(Optional.empty());

        // When Then
        assertAll(() -> {
                    assertThrows(LoanNotExistsException.class,
                            () -> underTest.createSchedule(loanScheduleEvent));
                },
                () -> {
                    verify(loanService, times(1)).calculateMonthlyInstallment(
                            loanScheduleEvent.amount(),
                            loanScheduleEvent.interestRate(),
                            loanScheduleEvent.numberOfInstallments());
                    verify(loanRepository, times(1))
                            .findById(loanScheduleEvent.loanId());
                });
    }

    @Test
    @DisplayName("It should throw LoanDetailsNotExistsException")
    void itShouldThrowLoanDetailsNotExistsException() {
        // Given
        when(loanService.calculateMonthlyInstallment(loanScheduleEvent.amount(),
                loanScheduleEvent.interestRate(),
                loanScheduleEvent.numberOfInstallments()))
                .thenReturn(BigDecimal.valueOf(300));
        when(loanRepository.findById(loanScheduleEvent.loanId()))
                .thenReturn(Optional.of(Loan.builder()
                        .id(loanScheduleEvent.loanId())
                        .build()));
        when(loanDetailsRepository.findByLoan_id(loanScheduleEvent.loanId()))
                .thenReturn(Optional.empty());
        // When Then
        assertAll(() -> {
                    assertThrows(LoanDetailsNotExistsException.class,
                            () -> underTest.createSchedule(loanScheduleEvent));
                },
                () -> {
                    verify(loanService, times(1)).calculateMonthlyInstallment(
                            loanScheduleEvent.amount(),
                            loanScheduleEvent.interestRate(),
                            loanScheduleEvent.numberOfInstallments());
                    verify(loanRepository, times(1))
                            .findById(loanScheduleEvent.loanId());
                    verify(loanDetailsRepository, times(1))
                            .findByLoan_id(loanScheduleEvent.loanId());
                    verify(loanScheduleRepository, times(1))
                            .saveAll(anyCollection());
                });
    }

    @Test
    @DisplayName("It should set payment status as PAID_ON_TIME")
    void itShouldSetPaymentStatusAsPaidOnTime() {
        // Given
        LoanSchedule loanSchedule = LoanSchedule.builder()
                .installment(new BigDecimal("500.00"))
                .paymentDate(LocalDate.now())
                .deadline(LocalDate.now().plusDays(30))
                .paymentStatus(UNPAID)
                .loan(Loan.builder().id(UUID.randomUUID().toString()).build())
                .build();

        when(loanScheduleRepository.findAllByLoan_idAndPaymentStatusOrPaymentStatus(
                updateSchedulePaymentEvent.loanId(),
                UNPAID,
                OVERDUE))
                .thenReturn(List.of(loanSchedule));

        // When
        underTest.updatePaymentInstallmentStatus(updateSchedulePaymentEvent);

        // Then
        assertAll(() -> {
            verify(loanScheduleRepository, times(1)).findAllByLoan_idAndPaymentStatusOrPaymentStatus(
                    updateSchedulePaymentEvent.loanId(),
                    UNPAID,
                    OVERDUE);
        });
    }

    @Test
    @DisplayName("It should set payment status as PAID_LATE")
    void itShouldSetPaymentStatusAsPaidLate() {
        // Given
        LoanSchedule loanSchedule = LoanSchedule.builder()
                .installment(new BigDecimal("500.00"))
                .paymentDate(LocalDate.now())
                .deadline(LocalDate.now().minusDays(30))
                .paymentStatus(OVERDUE)
                .loan(Loan.builder().id(UUID.randomUUID().toString()).build())
                .build();

        when(loanScheduleRepository.findAllByLoan_idAndPaymentStatusOrPaymentStatus(
                updateSchedulePaymentEvent.loanId(),
                UNPAID,
                OVERDUE))
                .thenReturn(List.of(loanSchedule));

        // When
        underTest.updatePaymentInstallmentStatus(updateSchedulePaymentEvent);

        // Then
        assertAll(() -> {
            verify(loanScheduleRepository, times(1)).findAllByLoan_idAndPaymentStatusOrPaymentStatus(
                    updateSchedulePaymentEvent.loanId(),
                    UNPAID,
                    OVERDUE);
        });
    }

    @Test
    @DisplayName("It should print log when is nothing to update")
    void itShouldPrintLogWhenIsNothingToUpdate() {
        // Given
        LoanSchedule loanSchedule = LoanSchedule.builder()
                .installment(new BigDecimal("500.00"))
                .paymentDate(LocalDate.now())
                .deadline(LocalDate.now().minusDays(30))
                .paymentStatus(OVERDUE)
                .loan(Loan.builder().id(UUID.randomUUID().toString()).build())
                .build();

        when(loanScheduleRepository.findAllByLoan_idAndPaymentStatusOrPaymentStatus(
                updateSchedulePaymentEvent.loanId(),
                UNPAID,
                OVERDUE))
                .thenReturn(Collections.emptyList());

        // When
        underTest.updatePaymentInstallmentStatus(updateSchedulePaymentEvent);

        // Then
        assertAll(() -> {
            verify(loanScheduleRepository, times(1)).findAllByLoan_idAndPaymentStatusOrPaymentStatus(
                    updateSchedulePaymentEvent.loanId(),
                    UNPAID,
                    OVERDUE);
        });
    }

    @Test
    @DisplayName("It should set payment status as OVERDUE")
    void itShouldSetPaymentStatusAsOverdue() {
        // Given
        when(loanScheduleRepository.setPaymentStatusFromUnpaidTo(OVERDUE)).thenReturn(3);

        // When
        underTest.setPaymentStatusAsPaidLate();
        ArgumentCaptor<PaymentStatus> paymentStatusArgumentCaptor = ArgumentCaptor.forClass(PaymentStatus.class);

        // Then
        assertAll(() -> {
            verify(loanScheduleRepository, times(1))
                    .setPaymentStatusFromUnpaidTo(paymentStatusArgumentCaptor.capture());
            assertEquals(OVERDUE, paymentStatusArgumentCaptor.getValue());
        });
    }

    @Test
    @DisplayName("It should return LoanSchedule fot given loanId")
    void itShouldReturnLoanScheduleForGivenLoanId() {
        // Given
        LocalDate deadline = LocalDate.now().minusDays(1);
        LoanSchedule loanSchedule = LoanSchedule.builder()
                .installment(new BigDecimal("500.00"))
                .deadline(deadline)
                .paymentStatus(PaymentStatus.UNPAID)
                .build();
        when(loanScheduleRepository.findAllByLoan_id(loanScheduleEvent.loanId()))
                .thenReturn(List.of(loanSchedule));
        // When
        List<LoanScheduleDto> loanSchedules = underTest.getLoanSchedule(loanScheduleEvent.loanId());

        // Then
        assertAll(() -> {
            verify(loanScheduleRepository, times(1))
                    .findAllByLoan_id(loanScheduleEvent.loanId());
            assertEquals(1, loanSchedules.size());
        });
    }
}