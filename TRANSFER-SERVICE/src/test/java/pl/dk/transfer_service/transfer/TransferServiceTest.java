package pl.dk.transfer_service.transfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pl.dk.transfer_service.exception.InsufficientBalanceException;
import pl.dk.transfer_service.httpClient.AccountFeignClient;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private AccountFeignClient accountFeignClient;
    private TransferService underTest;
    private AutoCloseable autoCloseable;

    AccountDto sender;
    AccountDto recipient;
    String senderUserId;
    String senderAccountNumber;
    String recipientAccountNumber;
    BigDecimal senderBalance;
    Boolean senderActiveStatus;
    LocalDateTime transferDate;
    String privacy;
    BigDecimal amount;
    String description;
    BigDecimal balanceAfterTransfer;

    CreateTransferDto createTransferDto;
    Transfer transfer;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new TransferServiceImpl(transferRepository, accountFeignClient);

        senderUserId = "63d520d6-df76-4ed7-a8a6-2f597248cfb1";
        senderAccountNumber = "00000000000000000000000000";
        recipientAccountNumber = "11111111111111111111111111";
        senderBalance = BigDecimal.valueOf(10000L);
        senderActiveStatus = true;
        transferDate = LocalDateTime.now();
        amount = new BigDecimal("500");
        privacy = "**********************";
        description = "Payment for invoice #12345";
        balanceAfterTransfer = senderBalance.subtract(amount);

        sender = AccountDto.builder()
                .accountNumber(senderAccountNumber)
                .balance(senderBalance)
                .userId(senderUserId)
                .active(senderActiveStatus)
                .build();

        recipient = AccountDto.builder()
                .accountNumber(recipientAccountNumber)
                .build();

        createTransferDto = CreateTransferDto.builder()
                .senderAccountNumber(senderAccountNumber)
                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .currencyType(CurrencyType.PLN.name())
                .transferDate(transferDate)
                .description("Monthly rent payment")
                .build();

        transfer = Transfer.builder()
                .id("123e4567-e89b-12d3-a456-426614174000")
                .senderAccountNumber(senderAccountNumber)
                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .currencyType(CurrencyType.PLN)
                .transferDate(transferDate)
                .transferStatus(TransferStatus.PENDING)
                .description(description)
                .balanceAfterTransfer(balanceAfterTransfer)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should create transfer successfully")
    void itShouldCreateTransferSuccessfully() {
        // Given
        Mockito.when(accountFeignClient.getAccountById(senderAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(sender)));
        Mockito.when(accountFeignClient.getAccountById(recipientAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(recipient)));
        Mockito.when(transferRepository.save(Mockito.any(Transfer.class))).thenReturn(transfer);

        // When
        TransferDto result = underTest.createTransfer(createTransferDto);
        ArgumentCaptor<Transfer> transferArgumentCaptor = ArgumentCaptor.forClass(Transfer.class);

        // Then
        assertAll(
                () -> {
                    Mockito.verify(accountFeignClient, Mockito.times(2)).getAccountById(Mockito.any(String.class));
                    Mockito.verify(transferRepository, Mockito.times(1)).save(transferArgumentCaptor.capture());
                    assertNotNull(result.transferId());
                    assertTrue(result.senderAccountNumber().contains(privacy), "The sender account number should be masked for privacy.");
                    assertTrue(result.recipientAccountNumber().contains(privacy), "The sender account number should be masked for privacy.");
                    assertEquals(amount, result.amount());
                    assertEquals(CurrencyType.PLN.toString(), result.currencyType());
                    assertEquals(transferDate, result.transferDate());
                    assertEquals(TransferStatus.PENDING.name(), result.transferStatus());
                    assertEquals(description, result.description());
                    assertEquals(balanceAfterTransfer, result.balanceAfterTransfer());
                }
        );
    }

    @Test
    @DisplayName("It should throw InsufficientBalanceException when send doesn't have enough funds ")
    void itShouldThrowInsufficientBalanceException() {
        // Given
        AccountDto mockSender = AccountDto.builder()
                .balance(BigDecimal.ZERO)
                .build();
        Mockito.when(accountFeignClient.getAccountById(senderAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(mockSender)));
        Mockito.when(accountFeignClient.getAccountById(recipientAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(recipient)));
        Mockito.when(transferRepository.save(Mockito.any(Transfer.class))).thenReturn(transfer);

        // When
        assertThrows(InsufficientBalanceException.class, () -> underTest.createTransfer(createTransferDto));

        // Then
        assertAll(
                () -> {
                    Mockito.verify(accountFeignClient, Mockito.times(2))
                            .getAccountById(Mockito.any(String.class));
                }
        );
    }
}