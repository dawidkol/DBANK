package pl.dk.loanservice.loan;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import pl.dk.loanservice.credit.CreditScoreCalculator;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.exception.UserNotFoundException;
import pl.dk.loanservice.httpClient.AccountServiceFeignClient;
import pl.dk.loanservice.httpClient.UserServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.UserDto;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;
import pl.dk.loanservice.loan.dtos.LoanEvent;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private ApplicationEventPublisher applicationEventPublisher;
    @Mock
    private AccountServiceFeignClient accountServiceFeignClient;
    @Mock
    private UserServiceFeignClient userServiceFeignClient;
    @Mock
    private CreditScoreCalculator creditScoreCalculator;

    private LoanService underTest;

    AutoCloseable autoCloseable;
    CreateLoanDto createLoanDto;
    String userId;
    BigDecimal amount;
    LocalDate startDate;
    LocalDate endDate;
    String description;
    BigDecimal avgIncome;
    BigDecimal avgExpenses;
    BigDecimal existingLoanRepayments;

    Loan loan;
    String id;
    BigDecimal interestRate;
    LoanStatus status;
    BigDecimal remainingAmount;

    UserDto userDto;
    String email;
    String phone;
    String firstName;
    String lastName;


    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new LoanServiceImpl(loanRepository, applicationEventPublisher, accountServiceFeignClient, userServiceFeignClient, creditScoreCalculator);
        userId = "550e8400-e29b-41d4-a716-446655440000";
        amount = BigDecimal.valueOf(5000);
        startDate = LocalDate.now().plusMonths(1);
        endDate = startDate.plusMonths(12);
        description = "Loan for purchasing new office equipment";
        avgIncome = BigDecimal.valueOf(10000);
        avgExpenses = BigDecimal.valueOf(8500);
        existingLoanRepayments = BigDecimal.ZERO;

        id = "123e4567-e89b-12d3-a456-426614174000";
        interestRate = BigDecimal.valueOf(4.5);
        status = LoanStatus.PENDING;
        remainingAmount = amount;

        email = "john@doe.com";
        phone = "+48666666666";
        firstName = "John";
        lastName = "Doe";

        createLoanDto = CreateLoanDto.builder()
                .userId(userId)
                .amount(amount)
                .interestRate(interestRate)
                .startDate(startDate)
                .endDate(endDate)
                .description(description)
                .avgIncome(avgIncome)
                .avgExpenses(avgExpenses)
                .existingLoanRepayments(existingLoanRepayments)
                .build();

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
                .build();

        userDto = UserDto.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should create loan successfully")
    void itShouldCreateLoanSuccessfully() {
        // Given
        Mockito.when(userServiceFeignClient.getUserById(userId)).thenReturn(ResponseEntity.of(Optional.of(userDto)));
        Mockito.when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        // When
        LoanDto result = underTest.createLoan(createLoanDto);

        // Then
        assertAll(() -> {
                    Mockito.verify(userServiceFeignClient, Mockito.times(1)).getUserById(userId);
                    Mockito.verify(loanRepository, Mockito.times(1)).save(any(Loan.class));
                    assertNotNull(result);
                    assertEquals(userId, result.userId());
                    assertEquals(amount, result.amount());
                    assertEquals(interestRate, result.interestRate());
                    assertEquals(startDate, result.startDate());
                    assertEquals(endDate, result.endDate());
                    assertEquals(remainingAmount, result.remainingAmount());
                    assertEquals(status.name(), result.status());
                    assertEquals(description, result.description());
                }
        );
    }

    @Test
    @DisplayName("It should throw UserNotFoundException when user with given id not exists")
    void itShouldThrowUserNotFoundExceptionWhenUserWithGivenIdNotExists() {
        // Given
        Mockito.when(userServiceFeignClient.getUserById(userId)).thenReturn(ResponseEntity.of(Optional.empty()));

        // When Then
        assertAll(() -> {
            assertThrows(UserNotFoundException.class, () -> underTest.createLoan(createLoanDto));
            Mockito.verify(userServiceFeignClient, Mockito.times(1)).getUserById(userId);
        });
    }

    @Test
    @DisplayName("It should get loan by given id successfully")
    void itShouldGetLoanByGivenIdSuccessfully() {
        // Given
        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        // When
        LoanDto result = underTest.getLoanById(id);

        // Then
        assertAll(() -> {
                    Mockito.verify(loanRepository, Mockito.times(1)).findById(id);
                    assertEquals(id, result.id());
                    assertEquals(userId, result.userId());
                    assertEquals(amount, result.amount());
                    assertEquals(interestRate, result.interestRate());
                    assertEquals(startDate, result.startDate());
                    assertEquals(endDate, result.endDate());
                    assertEquals(remainingAmount, result.remainingAmount());
                    assertEquals(status.name(), result.status());
                    assertEquals(description, result.description());
                }
        );
    }

    @Test
    @DisplayName("It should throw LoanNotExistsException when loan not exists")
    void itShouldThrowLoanNotExistsExceptionWhenLoanNotExists() {
        // Given
        Mockito.when(loanRepository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows(LoanNotExistsException.class, () -> underTest.getLoanById(id));

        // Then
        assertAll(() -> {
                    Mockito.verify(loanRepository, Mockito.times(1)).findById(id);
                }
        );
    }


    @Test
    @DisplayName("It should evaluate credit worthiness successfully and set loan status as APPROVED")
    void itShouldEvaluateCreditWorthinessSuccessfullyAndSetLoanStatusAsApproved() {
        // Given
        BigDecimal last12Months = BigDecimal.valueOf(1500);
        BigDecimal score = last12Months.add(BigDecimal.valueOf(2000));
        Mockito.when(accountServiceFeignClient.getAvgLast12Moths(userId)).thenReturn(ResponseEntity.of(Optional.of(last12Months)));
        Mockito.when(creditScoreCalculator.calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments)).thenReturn(score);
        Mockito.doNothing().when(loanRepository).updateLoansStatus(Mockito.any(LoanStatus.class), anyString());

        long totalMonths = Period.between(startDate, endDate).toTotalMonths();
        int months = (int) totalMonths;
        LoanEvent loanEvent = LoanEvent.builder()
                .userId(userId)
                .loanId(id)
                .avgIncome(avgIncome)
                .avgExpenses(avgExpenses)
                .existingLoanRepayment(existingLoanRepayments)
                .amountOfLoan(amount)
                .interestRate(interestRate)
                .months(months)
                .build();

        // When
        underTest.evaluateCreditWorthiness(loanEvent);

        // Then
        assertAll(() -> {
            Mockito.verify(accountServiceFeignClient, Mockito.times(1)).getAvgLast12Moths(userId);
            Mockito.verify(creditScoreCalculator, Mockito.times(1)).calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments);
            Mockito.verify(loanRepository, Mockito.times(1)).updateLoansStatus(Mockito.any(LoanStatus.class), anyString());
        });
    }

    @Test
    @DisplayName("It should evaluate credit worthiness successfully and set loan status as REJECTED")
    void itShouldEvaluateCreditWorthinessSuccessfullyAndSetLoanStatusAsRejected() {
        // Given
        BigDecimal last12Months = BigDecimal.valueOf(100);
        BigDecimal score = last12Months.add(BigDecimal.valueOf(0));
        Mockito.when(accountServiceFeignClient.getAvgLast12Moths(userId)).thenReturn(ResponseEntity.of(Optional.of(last12Months)));
        Mockito.when(creditScoreCalculator.calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments)).thenReturn(score);
        Mockito.doNothing().when(loanRepository).updateLoansStatus(Mockito.any(LoanStatus.class), anyString());

        long totalMonths = Period.between(startDate, endDate).toTotalMonths();
        int months = (int) totalMonths;
        LoanEvent loanEvent = LoanEvent.builder()
                .userId(userId)
                .loanId(id)
                .avgIncome(avgIncome)
                .avgExpenses(avgExpenses)
                .existingLoanRepayment(existingLoanRepayments)
                .amountOfLoan(amount)
                .interestRate(interestRate)
                .months(months)
                .build();

        // When
        underTest.evaluateCreditWorthiness(loanEvent);

        // Then
        assertAll(() -> {
            Mockito.verify(accountServiceFeignClient, Mockito.times(1)).getAvgLast12Moths(userId);
            Mockito.verify(creditScoreCalculator, Mockito.times(1)).calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments);
            Mockito.verify(loanRepository, Mockito.times(1)).updateLoansStatus(Mockito.any(LoanStatus.class), anyString());
        });
    }

    @Test
    @DisplayName("It should throw UserNotFoundException when user with given id doesn't have any account in USER-SERVICE")
    void itShouldThrowUserNotFoundExceptionWhenUserWithGivenIdDoesntHaveAnyAccountInUserService() {
        // Given
        LoanEvent loanEvent = LoanEvent.builder()
                .userId(userId)
                .build();
        Mockito.when(accountServiceFeignClient.getAvgLast12Moths(userId)).thenReturn(ResponseEntity.of(Optional.empty()));

        // When Then
        assertAll(() -> {
            assertThrows(UserNotFoundException.class, () -> underTest.evaluateCreditWorthiness(loanEvent));
            Mockito.verify(accountServiceFeignClient, Mockito.times(1)).getAvgLast12Moths(userId);
        });
    }
}