package pl.dk.loanservice.loan;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    private LoanService underTest;

    AutoCloseable autoCloseable;
    CreateLoanDto createLoanDto;
    String userId;
    BigDecimal amount;
    LocalDate startDate;
    LocalDate endDate;
    String description;

    Loan loan;
    String id;
    BigDecimal interestRate;
    LoanStatus status;
    BigDecimal remainingAmount;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new LoanServiceImpl(loanRepository);
        userId = "550e8400-e29b-41d4-a716-446655440000";
        amount = BigDecimal.valueOf(5000);
        startDate = LocalDate.now();
        endDate = LocalDate.now().plusMonths(12);
        description = "Loan for purchasing new office equipment";
        id = "123e4567-e89b-12d3-a456-426614174000";
        interestRate = BigDecimal.valueOf(4.5);
        status = LoanStatus.PENDING;
        remainingAmount = amount;

        createLoanDto = CreateLoanDto.builder()
                .userId(userId)
                .amount(amount)
                .interestRate(interestRate)
                .startDate(startDate)
                .endDate(endDate)
                .description(description)
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
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should create loan successfully")
    void itShouldCreateLoanSuccessfully() {
        // Given
        Mockito.when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        // When
        LoanDto result = underTest.createLoan(createLoanDto);

        // Then
        assertAll(() -> {
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
}