package pl.dk.loanservice.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.httpClient.AccountServiceFeignClient;
import pl.dk.loanservice.httpClient.TransferServiceFeignClient;
import pl.dk.loanservice.httpClient.UserServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.UserDto;
import pl.dk.loanservice.loan.dtos.*;
import pl.dk.loanservice.loan_details.LoanDetails;
import pl.dk.loanservice.loan_details.LoanDetailsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static pl.dk.loanservice.constants.PagingAndSorting.DEFAULT_PAGE;
import static pl.dk.loanservice.constants.PagingAndSorting.DEFAULT_SIZE;
import static pl.dk.loanservice.kafka.KafkaConstants.LOAN_ACCOUNT_CREATED;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false"})
@Transactional
@EmbeddedKafka(topics = LOAN_ACCOUNT_CREATED)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class LoanControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private UserServiceFeignClient userServiceFeignClient;

    @MockitoBean
    private TransferServiceFeignClient transferServiceFeignClient;

    @MockitoBean
    private AccountServiceFeignClient accountServiceFeignClient;

    @MockitoBean
    private LoanDetailsRepository loanDetailsRepository;


    CreateLoanDto createLoanDto;
    UserDto userDto;

    @BeforeEach
    void setUp() {
        createLoanDto = CreateLoanDto.builder()
                .userId("550e8400-e29b-41d4-a716-446655440000") // Example UUID
                .amount(new BigDecimal("10000.00")) // Loan amount
                .interestRate(new BigDecimal("5.5")) // Interest rate in percentage
                .startDate(LocalDate.now()) // Loan start date
                .endDate(LocalDate.now().plusYears(1)) // Loan end date (1 year later)
                .description("Loan for purchasing a new car.") // Description
                .avgIncome(new BigDecimal("5000.00")) // Average monthly income
                .avgExpenses(new BigDecimal("2000.00")) // Average monthly expenses
                .existingLoanRepayments(new BigDecimal("300.00")) // Existing loan repayments
                .build();

        userDto = UserDto.builder()
                .userId(createLoanDto.userId())
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .phone("+48666666666")
                .build();
    }

    @Test
    @DisplayName("Test Loan lifecycle and payment integration")
    void testLoanLifecycleAndPaymentIntegration() {
        // 1. User tries to create loan. Expected status: 201 CREATED
        // Given
        Mockito.when(userServiceFeignClient.getUserById(createLoanDto.userId())).thenReturn(ResponseEntity.of(Optional.of(userDto)));

        // When
        ResponseEntity<LoanDto> loanDtoResponseEntity = testRestTemplate.postForEntity("/loans", new HttpEntity<>(createLoanDto), LoanDto.class);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.CREATED, loanDtoResponseEntity.getStatusCode());
            assertNotNull(loanDtoResponseEntity.getBody());
        });

        // 2. User wants to get loan by given id. Expected status code: 200 OK
        // Given
        String stringUrl = loanDtoResponseEntity.getHeaders().getLocation().toString();
        String location = stringUrl.substring(stringUrl.lastIndexOf("/") + 1);

        // When
        ResponseEntity<LoanDto> forLoanDto = testRestTemplate.getForEntity("/loans/{loanId}", LoanDto.class, location);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.OK, forLoanDto.getStatusCode());
            assertNotNull(loanDtoResponseEntity.getBody());
        });

        // 3. User wants to calculate monthly installment. Expected status code: 200 OK
        // Given
        BigDecimal amount = BigDecimal.valueOf(3_000_000);
        BigDecimal months = BigDecimal.valueOf(30 * 12);
        BigDecimal interestRate = BigDecimal.valueOf(8);

        // When
        ResponseEntity<BigDecimal> getMonthlyInstallment = testRestTemplate.getForEntity(
                "/loans/monthly-installment?amount={amount}&interestRate={interestRate}&months={amountOfInstallemnts}",
                BigDecimal.class,
                amount, interestRate, months
        );

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.OK, getMonthlyInstallment.getStatusCode());
            assertNotNull(loanDtoResponseEntity.getBody());
        });

        // 4. User wants to get all his loans. Expected status code 200 OK
        // Given
        String userId = loanDtoResponseEntity.getBody().userId();

        // When
        ResponseEntity<List<LoanDto>> getAllUserLoans = testRestTemplate.exchange(
                "/loans/{userId}/all?page={page}&size={size}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<LoanDto>>() {
                },
                userId,
                DEFAULT_PAGE,
                DEFAULT_SIZE
        );

        // Then
        assertAll(() -> {
                    assertEquals(HttpStatus.OK, getAllUserLoans.getStatusCode());
                    assertNotNull(getAllUserLoans);
                    assertEquals(1, getAllUserLoans.getBody().size());
                }
        );

        // 5. User wants to pay for one of his loans
        // Given
        String senderAccountNumber = "00000000000000000000000000";
        String recipientAccountNumber = "11111111111111111111111111";
        CreateLoanInstallmentTransfer createLoanInstallmentTransfer = CreateLoanInstallmentTransfer.builder()
                .loanId(location)
                .senderAccountNumber(senderAccountNumber)
                .transferDate(LocalDateTime.now().plusMonths(3))
                .description("Monthly rent payment")
                .build();

        TransferDto transferDto = TransferDto.builder()
                .recipientAccountNumber(recipientAccountNumber)
                .senderAccountNumber(senderAccountNumber)
                .amount(amount)
                .build();

        LoanDetails loanDetails = LoanDetails.builder()
                .loanAccountNumber(recipientAccountNumber)
                .loan(Loan.builder().id(location).build())
                .build();

        Mockito.when(loanDetailsRepository.findByLoan_id(any())).thenReturn(Optional.of(loanDetails));
        Mockito.when(accountServiceFeignClient.getAvgLast12Moths(userId)).thenReturn(ResponseEntity.of(Optional.of(BigDecimal.valueOf(15000))));
        Mockito.when(transferServiceFeignClient.createTransfer(any())).thenReturn(ResponseEntity.status(201).body(transferDto));

        // When
        ResponseEntity<TransferDto> transferDtoResponseEntity = testRestTemplate.postForEntity("/loans/pay", new HttpEntity<>(createLoanInstallmentTransfer), TransferDto.class);

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.CREATED, transferDtoResponseEntity.getStatusCode());
        });

    }
}