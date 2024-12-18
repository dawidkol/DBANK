package pl.dk.accounts_service.account;

import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import pl.dk.accounts_service.account.dtos.AccountNumberDto;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;

class AccountNumberGeneratorTest {

    @Mock
    private AccountRepository accountRepository;
    private AutoCloseable autoCloseable;
    private AccountNumberGenerator underTest;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new AccountNumberGeneratorImpl(accountRepository);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should generate account number successfully")
    void itShouldGenerateAccountNumberSuccessfully() {
        // Given
        Mockito.when(accountRepository.findByAccountNumber(any(String.class))).thenReturn(Optional.empty());

        // When
        String result = underTest.generateAccountNumber();
        String s = String.valueOf(result);
        int accountLength = s.length();

        // Then
        Mockito.verify(accountRepository, times(1)).findByAccountNumber(any(String.class));
        assertThat(result).isInstanceOf(String.class);
        assertEquals(26, accountLength);
    }

    @Test
    @DisplayName("It should reiterate generation process due to account number conflict")
    void itShouldReiterateGenerationProcessDueToAccountNumberConflict() {
        // Given
        String accountNumber ="123456789012345678901234";
        AccountNumberDto accountNumberDto = new AccountNumberDto(accountNumber);
        Mockito.when(accountRepository.findByAccountNumber(any(String.class)))
                .thenReturn(Optional.of(accountNumberDto))
                .thenReturn(Optional.empty());

        // When
        String result = underTest.generateAccountNumber();

        // Then
        Mockito.verify(accountRepository, times(2)).findByAccountNumber(any(String.class));
        assertThat(result).isInstanceOf(String.class);
        assertThat(result.length()).isEqualTo(26);
    }
}