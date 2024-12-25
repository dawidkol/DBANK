package pl.dk.loanservice.loan;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.httpClient.UserServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.UserDto;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"eureka.client.enabled=false"})
@Transactional
class LoanControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private UserServiceFeignClient userServiceFeignClient;

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
    @DisplayName("Loan Service Integration Test: Create Loan, Retrieve Loan by ID, and Calculate Monthly Installment")
    void itShouldTestLoanLifecycleAndInstallmentCalculation() {
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
        BigDecimal months = BigDecimal.valueOf(30*12);
        BigDecimal interestRate = BigDecimal.valueOf(8);

        // When
        ResponseEntity<BigDecimal> getMonthlyInstallment = testRestTemplate.getForEntity(
                "/loans/monthly-installment?amount={amount}&interestRate={interestRate}&months={months}",
                BigDecimal.class,
                amount, interestRate, months
        );

        // Then
        assertAll(() -> {
            assertEquals(HttpStatus.OK, getMonthlyInstallment.getStatusCode());
            assertNotNull(loanDtoResponseEntity.getBody());
        });

    }
}