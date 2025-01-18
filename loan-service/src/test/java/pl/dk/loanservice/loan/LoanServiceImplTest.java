package pl.dk.loanservice.loan;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.server.ResponseStatusException;
import pl.dk.loanservice.credit.CreditScoreCalculator;
import pl.dk.loanservice.enums.CurrencyType;
import pl.dk.loanservice.enums.LoanStatus;
import pl.dk.loanservice.exception.*;
import pl.dk.loanservice.httpClient.AccountServiceFeignClient;
import pl.dk.loanservice.httpClient.TransferServiceFeignClient;
import pl.dk.loanservice.httpClient.UserServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.UserDto;
import pl.dk.loanservice.loan.dtos.*;
import pl.dk.loanservice.loan_details.LoanDetails;
import pl.dk.loanservice.loan_details.LoanDetailsRepository;
import pl.dk.loanservice.loan_schedule.LoanSchedule;
import pl.dk.loanservice.loan_schedule.LoanScheduleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static pl.dk.loanservice.constants.PagingAndSorting.*;
import static pl.dk.loanservice.constants.PagingAndSorting.DEFAULT_SIZE;
import static pl.dk.loanservice.enums.PaymentStatus.UNPAID;

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
    @Mock
    private KafkaTemplate<String, CreateLoanAccountDto> kafkaTemplate;
    @Mock
    private TransferServiceFeignClient transferServiceFeignClient;
    @Mock
    private LoanDetailsRepository loanDetailsRepository;
    @Mock
    private LoanScheduleRepository loanScheduleRepository;

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
    Integer numberOfInstallments;

    UserDto userDto;
    String email;
    String phone;
    String firstName;
    String lastName;

    String loanScheduleId;

    LoanDetails loanDetails;
    CreateTransferDto createTransferDto;
    CreateLoanInstallmentTransfer createLoanInstallmentTransfer;
    LoanSchedule loanSchedule;


    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new LoanServiceImpl(
                loanRepository,
                applicationEventPublisher,
                accountServiceFeignClient,
                userServiceFeignClient,
                creditScoreCalculator,
                kafkaTemplate,
                transferServiceFeignClient,
                loanDetailsRepository,
                loanScheduleRepository);
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
        numberOfInstallments = 24;

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
                .currencyType(CurrencyType.PLN.name())
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
                .numberOfInstallments(numberOfInstallments)
                .currencyType(CurrencyType.PLN)
                .build();

        userDto = UserDto.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .phone(phone)
                .build();


        String accountNumber = "65432109876543210987654321";
        loanDetails = LoanDetails.builder()
                .id(UUID.randomUUID().toString())
                .loan(loan)
                .loanAccountNumber(accountNumber)
                .build();

        loanSchedule = LoanSchedule.builder()
                .id(loanScheduleId)
                .paymentStatus(UNPAID)
                .paymentDate(LocalDate.now())
                .loan(loan)
                .build();

        String senderAccountNumber = "12345678901234567890123456";
        String descriptionForTransferDto = "Payment for loan";
        createTransferDto = CreateTransferDto.builder()
                .senderAccountNumber(senderAccountNumber)
                .recipientAccountNumber(accountNumber)
                .amount(BigDecimal.valueOf(5000.00))
                .currencyType("USD")
                .transferDate(LocalDateTime.now().plusMonths(1))
                .description(descriptionForTransferDto)
                .build();

        createLoanInstallmentTransfer = CreateLoanInstallmentTransfer.builder()
                .senderAccountNumber(senderAccountNumber)
                .transferDate(LocalDateTime.now().plusMonths(1))
                .description(descriptionForTransferDto)
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
        when(userServiceFeignClient.getUserById(userId))
                .thenReturn(ResponseEntity.of(Optional.of(userDto)));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        // When
        LoanDto result = underTest.createLoan(createLoanDto);

        // Then
        assertAll(() -> {
                    verify(userServiceFeignClient, times(1)).getUserById(userId);
                    verify(loanRepository, times(1)).save(any(Loan.class));
                    assertNotNull(result);
                    assertEquals(userId, result.userId());
                    assertEquals(amount, result.amount());
                    assertEquals(interestRate, result.interestRate());
                    assertEquals(startDate, result.startDate());
                    assertEquals(endDate, result.endDate());
                    assertEquals(remainingAmount, result.remainingAmount());
                    assertEquals(status.name(), result.status());
                    assertEquals(description, result.description());
                    assertEquals(createLoanDto.currencyType(), result.currencyType());
                }
        );
    }

    @Test
    @DisplayName("It should throw UserNotFoundException when user with given id not exists")
    void itShouldThrowUserNotFoundExceptionWhenUserWithGivenIdNotExists() {
        // Given
        when(userServiceFeignClient.getUserById(userId)).thenReturn(ResponseEntity.of(Optional.empty()));

        // When Then
        assertAll(() -> {
            assertThrows(UserNotFoundException.class, () -> underTest.createLoan(createLoanDto));
            verify(userServiceFeignClient, times(1)).getUserById(userId);
        });
    }

    @Test
    @DisplayName("It should get loan by given id successfully")
    void itShouldGetLoanByGivenIdSuccessfully() {
        // Given
        when(loanRepository.findById(id)).thenReturn(Optional.of(loan));

        // When
        LoanDto result = underTest.getLoanById(id);

        // Then
        assertAll(() -> {
                    verify(loanRepository, times(1)).findById(id);
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
        when(loanRepository.findById(id)).thenReturn(Optional.empty());

        // When
        assertThrows(LoanNotExistsException.class, () -> underTest.getLoanById(id));

        // Then
        assertAll(() -> {
                    verify(loanRepository, times(1)).findById(id);
                }
        );
    }


    @Test
    @DisplayName("It should evaluate credit worthiness successfully and set loan status as APPROVED")
    void itShouldEvaluateCreditWorthinessSuccessfullyAndSetLoanStatusAsApproved() {
        // Given
        BigDecimal last12Months = BigDecimal.valueOf(1500);
        BigDecimal score = last12Months.add(BigDecimal.valueOf(2000));
        when(accountServiceFeignClient.getAvgLast12Moths(userId)).thenReturn(ResponseEntity.of(Optional.of(last12Months)));
        when(creditScoreCalculator.calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments)).thenReturn(score);
        doNothing().when(loanRepository).updateLoansStatus(any(LoanStatus.class), anyString());

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
                .numberOfInstallments(months)
                .build();

        // When
        underTest.evaluateCreditWorthiness(loanEvent);

        // Then
        assertAll(() -> {
            verify(accountServiceFeignClient, times(1)).getAvgLast12Moths(userId);
            verify(creditScoreCalculator, times(1)).calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments);
            verify(loanRepository, times(1)).updateLoansStatus(any(LoanStatus.class), anyString());
        });
    }

    @Test
    @DisplayName("It should evaluate credit worthiness successfully and set loan status as REJECTED")
    void itShouldEvaluateCreditWorthinessSuccessfullyAndSetLoanStatusAsRejected() {
        // Given
        BigDecimal last12Months = BigDecimal.valueOf(100);
        BigDecimal score = last12Months.add(BigDecimal.valueOf(0));
        when(accountServiceFeignClient.getAvgLast12Moths(userId)).thenReturn(ResponseEntity.of(Optional.of(last12Months)));
        when(creditScoreCalculator.calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments)).thenReturn(score);
        doNothing().when(loanRepository).updateLoansStatus(any(LoanStatus.class), anyString());

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
                .numberOfInstallments(months)
                .build();

        // When
        underTest.evaluateCreditWorthiness(loanEvent);

        // Then
        assertAll(() -> {
            verify(accountServiceFeignClient, times(1)).getAvgLast12Moths(userId);
            verify(creditScoreCalculator, times(1)).calculateCreditScore(avgIncome, avgExpenses, last12Months, existingLoanRepayments);
            verify(loanRepository, times(1)).updateLoansStatus(any(LoanStatus.class), anyString());
        });
    }

    @Test
    @DisplayName("It should throw UserNotFoundException when user with given id doesn't have any account in USER-SERVICE")
    void itShouldThrowUserNotFoundExceptionWhenUserWithGivenIdDoesntHaveAnyAccountInUserService() {
        // Given
        LoanEvent loanEvent = LoanEvent.builder()
                .userId(userId)
                .build();
        when(accountServiceFeignClient.getAvgLast12Moths(userId)).thenReturn(ResponseEntity.of(Optional.empty()));

        // When Then
        assertAll(() -> {
            assertThrows(UserNotFoundException.class, () -> underTest.evaluateCreditWorthiness(loanEvent));
            verify(accountServiceFeignClient, times(1)).getAvgLast12Moths(userId);
        });
    }

    @Test
    @DisplayName("it should return all user loans")
    void itShouldReturnAllUserLoans() {
        // Given
        Loan loan1 = Loan.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .amount(BigDecimal.valueOf(10000.00))
                .interestRate(BigDecimal.valueOf(3.5))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusYears(5))
                .remainingAmount(BigDecimal.valueOf(10000.00))
                .status(LoanStatus.PENDING)
                .description("Personal loan for home renovation.")
                .currencyType(CurrencyType.PLN)
                .build();

        Loan loan2 = Loan.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .amount(BigDecimal.valueOf(5000.00))
                .interestRate(BigDecimal.valueOf(4.2))
                .startDate(LocalDate.now().minusMonths(3))
                .endDate(LocalDate.now().plusYears(2))
                .remainingAmount(BigDecimal.valueOf(4500.00))
                .status(LoanStatus.ACTIVE)
                .description("Short-term loan for car repair expenses.")
                .currencyType(CurrencyType.PLN)
                .build();

        Loan loan3 = Loan.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .amount(BigDecimal.valueOf(25000.00))
                .interestRate(BigDecimal.valueOf(5.0))
                .startDate(LocalDate.now().minusYears(1))
                .endDate(LocalDate.now().plusYears(10))
                .remainingAmount(BigDecimal.valueOf(20000.00))
                .status(LoanStatus.ACTIVE)
                .description("Long-term loan for business expansion.")
                .currencyType(CurrencyType.PLN)
                .build();

        int page = Integer.parseInt(DEFAULT_PAGE);
        int size = Integer.parseInt(DEFAULT_SIZE);

        PageRequest pageRequest = PageRequest.of(page - 1, size);
        PageImpl<Loan> pageImpl = new PageImpl<>(List.of(loan1, loan2, loan3));

        when(loanRepository.findAllByUserId(userId, pageRequest)).thenReturn(pageImpl);

        // When
        List<LoanDto> result = underTest.getAllUsersLoans(userId, page, size);

        // Then
        assertAll(() -> {
            verify(loanRepository, times(1)).findAllByUserId(userId, pageRequest);
            assertEquals(3, result.size());
            assertThat(result).hasOnlyElementsOfType(LoanDto.class);
        });
    }

    @Test
    @DisplayName("It should pay installment successfully")
    void itShouldPayInstallmentSuccessfully() {
        // Given
        when(loanScheduleRepository.findById(loanScheduleId))
                .thenReturn(Optional.of(loanSchedule));
        when(loanDetailsRepository.findByLoan_id(loan.getId()))
                .thenReturn(Optional.of(loanDetails));
        when(transferServiceFeignClient.createTransfer(any()))
                .thenReturn(ResponseEntity.status(201)
                        .body(TransferDto.builder()
                                .transferId(UUID.randomUUID().toString())
                                .transferDate(LocalDateTime.now())
                                .build()));

        // When
        underTest.payInstallment(loanScheduleId, createLoanInstallmentTransfer);

        // Then
        assertAll(() -> {
            verify(loanScheduleRepository, times(1))
                    .findById(loanScheduleId);
            verify(loanDetailsRepository, times(1))
                    .findByLoan_id(any());
            verify(transferServiceFeignClient, times(1))
                    .createTransfer(any());
        });
    }

    @Test
    @DisplayName("It should throw LoanScheduleNotExists exception")
    void itShouldThrowLoanScheduleNotExistsException() {
        // Given
        when(loanScheduleRepository.findById(loan.getId()))
                .thenReturn(Optional.empty());

        // When Then
        assertAll(() -> {
            assertThrows(LoanScheduleNotExistsException.class,
                    () -> underTest.payInstallment(loanScheduleId, createLoanInstallmentTransfer));
            verify(loanScheduleRepository, times(1))
                    .findById(loanScheduleId);
        });
    }

    @Test
    @DisplayName("It should throw LoanDetailsNotExistsException ")
    void itShouldThrowLoanDetailsNotExistsException() {
        // Given
        when(loanScheduleRepository.findById(loanScheduleId))
                .thenReturn(Optional.of(loanSchedule));
        when(loanDetailsRepository.findByLoan_id(loan.getId()))
                .thenReturn(Optional.empty());

        // When Then
        assertAll(() -> {
            assertThrows(LoanDetailsNotExistsException.class,
                    () -> underTest.payInstallment(loanScheduleId, createLoanInstallmentTransfer));
            verify(loanScheduleRepository, times(1))
                    .findById(loanScheduleId);
            verify(loanDetailsRepository, times(1))
                    .findByLoan_id(loan.getId());
        });
    }

    @Test
    @DisplayName("It should throw PayInstallmentException")
    void itShouldThrowPayInstallmentException() {
        // Given
        when(loanScheduleRepository.findById(loanScheduleId))
                .thenReturn(Optional.of(loanSchedule));
        when(loanDetailsRepository.findByLoan_id(loan.getId()))
                .thenReturn(Optional.of(loanDetails));
        when(transferServiceFeignClient.createTransfer(any()))
                .thenReturn(ResponseEntity.status(100).build());

        // When Then
        assertAll(() -> {
            assertThrows(PayInstallmentException.class,
                    () -> underTest.payInstallment(loanScheduleId, createLoanInstallmentTransfer));
            verify(loanScheduleRepository, times(1))
                    .findById(loanScheduleId);
            verify(loanDetailsRepository, times(1))
                    .findByLoan_id(any());
            verify(transferServiceFeignClient, times(1))
                    .createTransfer(any());
        });
    }

    @Test
    @DisplayName("It should throw ResponseStatusException: 400 BAD_REQUEST")
    void itShouldThrowResponseStatusException404BadRequest() {
        // Given
        when(loanScheduleRepository.findById(loanScheduleId))
                .thenReturn(Optional.of(loanSchedule));
        when(loanDetailsRepository.findByLoan_id(loan.getId()))
                .thenReturn(Optional.of(loanDetails));
        when(transferServiceFeignClient.createTransfer(any()))
                .thenReturn(ResponseEntity.status(400).build());

        // When Then
        assertAll(() -> {
            assertThrows(ResponseStatusException.class,
                    () -> underTest.payInstallment(loanScheduleId, createLoanInstallmentTransfer));
            verify(loanScheduleRepository, times(1))
                    .findById(loanScheduleId);
            verify(loanDetailsRepository, times(1))
                    .findByLoan_id(any());
            verify(transferServiceFeignClient, times(1))
                    .createTransfer(any());
        });
    }

    @DisplayName("It should throw TransferServiceUnavailableException")
    @ParameterizedTest
    @ValueSource(ints = {500, 501, 502, 503, 504})
    void itShouldThrowTransferServiceUnavailable(int statusCode) {
        // Given
        when(loanScheduleRepository.findById(loanScheduleId))
                .thenReturn(Optional.of(loanSchedule));
        when(loanDetailsRepository.findByLoan_id(loan.getId()))
                .thenReturn(Optional.of(loanDetails));
        when(transferServiceFeignClient.createTransfer(any()))
                .thenReturn(ResponseEntity.status(statusCode).build());

        // When Then
        assertAll(() -> {
            assertThrows(TransferServiceUnavailableException.class,
                    () -> underTest.payInstallment(loanScheduleId, createLoanInstallmentTransfer));
            verify(loanScheduleRepository, times(1))
                    .findById(loanScheduleId);
            verify(loanDetailsRepository, times(1))
                    .findByLoan_id(any());
            verify(transferServiceFeignClient, times(1)).createTransfer(any());
        });
    }


}