package pl.dk.loanservice.loan_details;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pl.dk.loanservice.exception.AccountNotExistsException;
import pl.dk.loanservice.exception.LoanDetailsNotExistsException;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.httpClient.AccountServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.AccountDto;
import pl.dk.loanservice.loan.Loan;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.enums.LoanStatus;
import pl.dk.loanservice.loan_details.dtos.LoanDetailsDto;
import pl.dk.loanservice.loan_schedule.LoanSchedule;
import pl.dk.loanservice.loan_schedule.LoanScheduleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class LoanDetailsServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private LoanDetailsRepository loanDetailsRepository;
    @Mock
    private LoanScheduleRepository loanScheduleRepository;
    @Mock
    private AccountServiceFeignClient accountServiceFeignClient;

    private LoanDetailsService underTest;

    AutoCloseable autoCloseable;

    Loan loan;
    LoanDetails loanDetails;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new LoanDetailsServiceImpl(loanRepository, loanDetailsRepository, loanScheduleRepository, accountServiceFeignClient);

        String userId = "550e8400-e29b-41d4-a716-446655440000";
        BigDecimal amount = BigDecimal.valueOf(5000);
        LocalDate startDate = LocalDate.now().plusMonths(1);
        LocalDate endDate = startDate.plusMonths(12);
        String description = "Loan for purchasing new office equipment";

        String id = "123e4567-e89b-12d3-a456-426614174000";
        BigDecimal interestRate = BigDecimal.valueOf(4.5);
        LoanStatus status = LoanStatus.PENDING;
        BigDecimal remainingAmount = amount;
        Integer numberOfInstallments = 24;

        loan = Loan.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .interestRate(interestRate)
                .startDate(startDate)
                .endDate(endDate)
                .remainingAmount(remainingAmount)
                .status(status)
                .description(description)
                .numberOfInstallments(numberOfInstallments)
                .build();

        loanDetails = LoanDetails.builder()
                .id(UUID.randomUUID().toString())
                .loan(loan)
                .loanAccountNumber("00000000000000000000000001")
                .scheduleAvailable(true)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should get LoanDetails by given LoanId")
    void itShouldGetLoanDetailsByGivenLoanId() {
        // Given
        Mockito.when(loanRepository.findById(loan.getId()))
                .thenReturn(Optional.of(loan));
        Mockito.when(loanDetailsRepository.findByLoan_id(loan.getId()))
                .thenReturn(Optional.of(loanDetails));
        Mockito.when(loanScheduleRepository.findFirstByLoan_Id(loan.getId()))
                .thenReturn(Optional.of(new LoanSchedule()));
        AccountDto accountDto = AccountDto.builder()
                .balance(BigDecimal.TEN)
                .build();
        Mockito.when(accountServiceFeignClient.getAccountById(loanDetails.getLoanAccountNumber()))
                .thenReturn(ResponseEntity.ok().body(
                        accountDto));

        // When
        LoanDetailsDto result = underTest.getLoanDetails(loan.getId());

        // Then
        assertAll(() -> {
                    verify(loanRepository, times(1)).findById(loan.getId());
                    verify(loanDetailsRepository, times(1))
                            .findByLoan_id(loan.getId());
                    verify(loanScheduleRepository, times(1))
                            .findFirstByLoan_Id(loan.getId());
                    verify(accountServiceFeignClient, times(1))
                            .getAccountById(loanDetails.getLoanAccountNumber());
                }, () -> {
                    assertEquals(loan.getId(), result.loanId());
                    assertEquals(loan.getUserId(), result.userId());
                    assertEquals(loan.getAmount(), result.amount());
                    assertEquals(loan.getAmount().subtract(accountDto.balance()), result.remainingAmount());
                    assertEquals(accountDto.balance(), result.amountPaid());
                    assertEquals(loanDetails.getLoanAccountNumber(), result.loanAccount());
                    assertEquals(loan.getEndDate(), result.lastPaymentDateTo());
                    assertEquals(loanDetails.getScheduleAvailable(), result.scheduleAvailable());
                }
        );
    }

    @Test
    @DisplayName("It should throw LoanNotExistsException")
    void itShouldThrowLoanNotExistsException() {
        // Given
        Mockito.when(loanRepository.findById(loan.getId())).thenReturn(Optional.empty());


        // When Then
        assertAll(() -> {
            assertThrows(LoanNotExistsException.class, () ->
                    underTest.getLoanDetails(loan.getId()));
            verify(loanRepository, times(1)).findById(loan.getId());
        });
    }

    @Test
    @DisplayName("It should throw LoanDetailsNotExistsException")
    void itShouldThrowLoanDetailsNotExistsException() {
        // Given
        Mockito.when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        Mockito.when(loanDetailsRepository.findByLoan_id(loan.getId())).thenReturn(Optional.empty());

        // When
        assertAll(() -> {
            assertThrows(LoanDetailsNotExistsException.class, () ->
                    underTest.getLoanDetails(loan.getId()));
            verify(loanRepository, times(1)).findById(loan.getId());
            verify(loanDetailsRepository, times(1)).findByLoan_id(loan.getId());
        });
    }

    @Test
    @DisplayName("It should throw AccountNotExistsException")
    void itShouldThrowAccountNotExistsException() {
        // Given
        Mockito.when(loanRepository.findById(loan.getId())).thenReturn(Optional.of(loan));
        Mockito.when(loanDetailsRepository.findByLoan_id(loan.getId())).thenReturn(Optional.of(loanDetails));
        Mockito.when(loanScheduleRepository.findFirstByLoan_Id(loan.getId()))
                .thenReturn(Optional.of(new LoanSchedule()));
        Mockito.when(accountServiceFeignClient.getAccountById(loanDetails.getLoanAccountNumber()))
                .thenReturn(ResponseEntity.status(404).build());

        // When Then
        assertAll(() -> {
                    assertThrows(AccountNotExistsException.class, () ->
                            underTest.getLoanDetails(loan.getId()));
                    verify(loanRepository, times(1))
                            .findById(loan.getId());
                    verify(loanDetailsRepository, times(1))
                            .findByLoan_id(loan.getId());
                    verify(loanScheduleRepository, times(1))
                            .findFirstByLoan_Id(loan.getId());
                    verify(accountServiceFeignClient, times(1))
                            .getAccountById(loanDetails.getLoanAccountNumber());
                }
        );
    }
}