package pl.dk.transfer_service.transfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import pl.dk.transfer_service.enums.CurrencyType;
import pl.dk.transfer_service.enums.TransferStatus;
import pl.dk.transfer_service.exception.InsufficientBalanceException;
import pl.dk.transfer_service.exception.TransferNotFoundException;
import pl.dk.transfer_service.exception.TransferStatusException;
import pl.dk.transfer_service.httpClient.AccountFeignClient;
import pl.dk.transfer_service.httpClient.dtos.AccountBalanceDto;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static pl.dk.transfer_service.constants.TransferServiceConstants.*;
import static pl.dk.transfer_service.enums.TransferStatus.*;

class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;
    @Mock
    private AccountFeignClient accountFeignClient;
    @Mock
    KafkaTemplate<String, TransferEvent> kafkaTemplate;
    private TransferService underTest;
    private AutoCloseable autoCloseable;
    private String transferScheduler;

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
        transferScheduler = "0 0 6 * * ?";
        underTest = new TransferServiceImpl(transferRepository, accountFeignClient, kafkaTemplate, transferScheduler);

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
                .transferStatus(PENDING)
                .description(description)
                .balanceAfterTransfer(balanceAfterTransfer)
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should create transfer successfully")
    void itShouldCreateTransferSuccessfully(CurrencyType currencyType) {
        // Given
        Transfer transfer = Transfer.builder()
                .id("123e4567-e89b-12d3-a456-426614174000")
                .senderAccountNumber(senderAccountNumber)
                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .currencyType(currencyType)
                .transferDate(transferDate)
                .transferStatus(PENDING)
                .description(description)
                .balanceAfterTransfer(balanceAfterTransfer)
                .build();

        Mockito.when(accountFeignClient.getAccountById(senderAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(sender)));
        Mockito.when(accountFeignClient.getAccountById(recipientAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(recipient)));
        Mockito.when(transferRepository.save(any(Transfer.class)))
                .thenReturn(transfer);

        Mockito.when(accountFeignClient.getAccountBalanceByAccountNumberAndCurrencyType(
                        senderAccountNumber,
                        currencyType.name()))
                .thenReturn(ResponseEntity.ok(
                        AccountBalanceDto.builder()
                                .balance(senderBalance)
                                .currencyType(currencyType)
                                .build()));

        CreateTransferDto createTransferDto = CreateTransferDto.builder()
                .senderAccountNumber(senderAccountNumber)
                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .currencyType(currencyType.name())
                .transferDate(transferDate)
                .description("Monthly rent payment")
                .build();

        // When
        TransferDto result = underTest.createTransfer(createTransferDto);
        ArgumentCaptor<Transfer> transferArgumentCaptor = ArgumentCaptor.forClass(Transfer.class);

        // Then
        assertAll(
                () -> {
                    Mockito.verify(accountFeignClient, Mockito.times(2))
                            .getAccountById(any(String.class));
                    Mockito.verify(transferRepository, Mockito.times(1))
                            .save(transferArgumentCaptor.capture());
                    assertNotNull(result.transferId());
                    assertTrue(result.senderAccountNumber().contains(privacy),
                            "The sender account number should be masked for privacy.");
                    assertTrue(result.recipientAccountNumber().contains(privacy),
                            "The sender account number should be masked for privacy.");
                    assertEquals(amount, result.amount());
                    assertEquals(currencyType.name(), result.currencyType());
                    assertEquals(transferDate, result.transferDate());
                    assertEquals(PENDING.name(), result.transferStatus());
                    assertEquals(description, result.description());
                    assertEquals(balanceAfterTransfer, result.balanceAfterTransfer());
                }
        );
    }

    @ParameterizedTest
    @EnumSource(CurrencyType.class)
    @DisplayName("It should throw InsufficientBalanceException when send doesn't have enough funds ")
    void itShouldThrowInsufficientBalanceException(CurrencyType currencyType) {
        // Given
        AccountDto mockSender = AccountDto.builder()
                .build();
        Mockito.when(accountFeignClient.getAccountById(senderAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(mockSender)));
        Mockito.when(accountFeignClient.getAccountById(recipientAccountNumber))
                .thenReturn(ResponseEntity.of(Optional.of(recipient)));

        Transfer transfer = Transfer.builder()
                .id("123e4567-e89b-12d3-a456-426614174000")
                .senderAccountNumber(senderAccountNumber)
                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .currencyType(currencyType)
                .transferDate(transferDate)
                .transferStatus(PENDING)
                .description(description)
                .balanceAfterTransfer(balanceAfterTransfer)
                .build();
        Mockito.when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);

        Mockito.when(accountFeignClient.getAccountBalanceByAccountNumberAndCurrencyType(
                        senderAccountNumber,
                        currencyType.name()))
                .thenReturn(ResponseEntity.ok(
                        AccountBalanceDto.builder()
                                .balance(BigDecimal.ZERO)
                                .currencyType(currencyType)
                                .build()));

        CreateTransferDto createTransferDto = CreateTransferDto.builder()
                .senderAccountNumber(senderAccountNumber)
                .recipientAccountNumber(recipientAccountNumber)
                .amount(amount)
                .currencyType(currencyType.name())
                .transferDate(transferDate)
                .description("Monthly rent payment")
                .build();

        // When
        assertThrows(InsufficientBalanceException.class, () -> underTest.createTransfer(createTransferDto));

        // Then
        assertAll(
                () -> {
                    Mockito.verify(accountFeignClient, Mockito.times(2))
                            .getAccountById(any(String.class));
                }
        );
    }

    @Test
    @DisplayName("It should return transfer by given Id")
    void itShouldReturnTransferByGivenId() {
        // Given
        Mockito.when(transferRepository.findById(transfer.getId()))
                .thenReturn(Optional.of(transfer));

        // When
        TransferDto result = underTest.getTransferById(transfer.getId());

        // Then
        assertAll(() -> {
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findById(transfer.getId());
            assertEquals(transfer.getId(), result.transferId());
            assertNotEquals(transfer.getSenderAccountNumber(), result.senderAccountNumber());
            assertNotEquals(transfer.getRecipientAccountNumber(), result.recipientAccountNumber());
            assertEquals(transfer.getAmount(), result.amount());
            assertEquals(transfer.getCurrencyType().name(), result.currencyType());
            assertEquals(transfer.getTransferDate(), result.transferDate());
            assertEquals(transfer.getTransferStatus().name(), result.transferStatus());
            assertEquals(transfer.getDescription(), result.description());
            assertEquals(transfer.getBalanceAfterTransfer(), result.balanceAfterTransfer());
        });
    }

    @Test
    @DisplayName("It should throw TransferNotFoundException when given id not exists")
    void itShouldThrowTransferNotFoundExceptionWhenGivenIdNotExists() {
        // Given
        Mockito.when(transferRepository.findById(transfer.getId()))
                .thenReturn(Optional.empty());

        // When // Then
        assertAll(() -> {
            assertThrows(TransferNotFoundException.class,
                    () -> underTest.getTransferById(transfer.getId()));
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findById(transfer.getId());
        });
    }

    @ParameterizedTest
    @EnumSource(TransferStatus.class)
    @DisplayName("It should set transfer status as COMPLETED")
    void itShouldSetTransferStatusAs(TransferStatus transferStatus) {
        // Given
        Mockito.when(transferRepository.findById(transfer.getId()))
                .thenReturn(Optional.of(transfer));

        // When
        underTest.updateTransferStatus(transfer.getId(), transferStatus);

        // Then
        assertAll(() -> {
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findById(transfer.getId());
        });
    }

    @Test
    void itShouldThrowExceptionWhenUserTriesToUpdateTransferStatusThatNotExists() {
        // Given
        Mockito.when(transferRepository.findById(transfer.getId()))
                .thenReturn(Optional.empty());

        // When Then
        assertAll(() -> {
            assertThrows(TransferNotFoundException.class, () ->
                    underTest.updateTransferStatus(transfer.getId(), COMPLETED));
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findById(transfer.getId());
        });
    }

    @Test
    @DisplayName("It should return all transfers from given account number")
    void itShouldReturnAllTransferFromGivenAccountNumber() {
        // Given
        List<Transfer> transfers = List.of(transfer);
        int pageNumber = Integer.parseInt(PAGE_DEFAULT);
        int pageSize = Integer.parseInt(SIZE_DEFAULT);
        Mockito.when(transferRepository.findAllBySenderAccountNumber(senderAccountNumber,
                        PageRequest.of(
                                pageNumber - 1,
                                pageSize)))
                .thenReturn(new PageImpl<>(transfers));

        // When
        List<TransferDto> result = underTest.getAllTransfersFromAccount(senderAccountNumber, pageNumber, pageSize);

        // Then
        assertAll(() -> {
            assertEquals(transfers.size(), result.size());
            assertThat(result).hasOnlyElementsOfType(TransferDto.class);
        });
    }

    @Test
    @DisplayName("It should return all transfers from given account number to another given account number")
    void itShouldReturnAllTransferFromGivenAccountToAnotherGivenAccount() {
        // Given
        List<Transfer> transfers = List.of(transfer);
        int pageNumber = Integer.parseInt(PAGE_DEFAULT);
        int pageSize = Integer.parseInt(SIZE_DEFAULT);
        Mockito.when(transferRepository.findAllBySenderAccountNumberAndRecipientAccountNumber(senderAccountNumber,
                        recipientAccountNumber,
                        PageRequest.of(
                                pageNumber - 1,
                                pageSize)))
                .thenReturn(new PageImpl<>(transfers));

        // When
        List<TransferDto> result = underTest.getAllTransferFromTo(senderAccountNumber, recipientAccountNumber, pageNumber, pageSize);

        // Then
        assertAll(() -> {
            assertEquals(transfers.size(), result.size());
            assertThat(result).hasOnlyElementsOfType(TransferDto.class);
        });
    }

    @Test
    @DisplayName("It should cancel scheduled transfer successfully")
    void itShouldCancelScheduledTransferSuccessfully() {
        // Given
        Transfer scheduledTransfer = Transfer.builder()
                .id(transfer.getId())
                .senderAccountNumber(transfer.getSenderAccountNumber())
                .recipientAccountNumber(transfer.getRecipientAccountNumber())
                .amount(transfer.getAmount())
                .currencyType(transfer.getCurrencyType())
                .transferDate(transfer.getTransferDate().plusDays(1))
                .transferStatus(SCHEDULED)
                .description(transfer.getDescription())
                .balanceAfterTransfer(transfer.getBalanceAfterTransfer())
                .build();
        Mockito.when(transferRepository.findByIdAndTransferStatus(scheduledTransfer.getId(), SCHEDULED))
                .thenReturn(Optional.of(scheduledTransfer));

        // When
        underTest.cancelScheduledTransfer(scheduledTransfer.getId());

        // Then
        assertAll(() -> {
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findByIdAndTransferStatus(scheduledTransfer.getId(), SCHEDULED);
        });
    }

    @Test
    @DisplayName("It should throw TransferStatusException when user tries to cancel transfer with invalid transfer date")
    void itShouldThrowTransferStatusExceptionWhenUserTriesToCancelTransferWithInvalidTransferDate() {
        // Given
        Transfer scheduledTransfer = Transfer.builder()
                .id(transfer.getId())
                .senderAccountNumber(transfer.getSenderAccountNumber())
                .recipientAccountNumber(transfer.getRecipientAccountNumber())
                .amount(transfer.getAmount())
                .currencyType(transfer.getCurrencyType())
                .transferDate(transfer.getTransferDate().minusDays(1))
                .transferStatus(SCHEDULED)
                .description(transfer.getDescription())
                .balanceAfterTransfer(transfer.getBalanceAfterTransfer())
                .build();
        Mockito.when(transferRepository.findByIdAndTransferStatus(scheduledTransfer.getId(), SCHEDULED))
                .thenReturn(Optional.of(scheduledTransfer));

        // When Then
        assertAll(() -> {
            assertThrows(TransferStatusException.class, () ->
                    underTest.cancelScheduledTransfer(scheduledTransfer.getId()));
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findByIdAndTransferStatus(scheduledTransfer.getId(), SCHEDULED);
        });
    }

    @Test
    @DisplayName("It should throw TransferNotFoundException when user tries to cancel transfer with invalid id")
    void itShouldThrowTransferNotFoundExceptionWhenUserTriesToCancelTransferWithInvalidId() {
        // Given
        Transfer transferWithId = Transfer.builder().id(UUID.randomUUID().toString()).build();
        Mockito.when(transferRepository.findByIdAndTransferStatus(transferWithId.getId(), SCHEDULED))
                .thenReturn(Optional.empty());

        // When Then
        assertAll(() -> {
            assertThrows(TransferNotFoundException.class, () ->
                    underTest.cancelScheduledTransfer(transferWithId.getId()));
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findByIdAndTransferStatus(transferWithId.getId(), SCHEDULED);
        });
    }

    @Test
    @DisplayName("It should execute scheduled transfers successfully")
    void itShouldExecuteScheduledTransfersSuccessfully() {
        // Given
        Transfer scheduledTransfer = Transfer.builder()
                .id(transfer.getId())
                .senderAccountNumber(transfer.getSenderAccountNumber())
                .recipientAccountNumber(transfer.getRecipientAccountNumber())
                .amount(transfer.getAmount())
                .currencyType(transfer.getCurrencyType())
                .transferDate(transfer.getTransferDate().plusDays(1))
                .transferStatus(SCHEDULED)
                .description(transfer.getDescription())
                .balanceAfterTransfer(transfer.getBalanceAfterTransfer())
                .build();
        Mockito.when(transferRepository.findAllByTransferStatusAndTransferDateBefore(eq(SCHEDULED),
                any(LocalDateTime.class))).thenReturn(List.of(scheduledTransfer));

        // When
        underTest.executeScheduledTransfers();
        ArgumentCaptor<LocalDateTime> localDateTimeArgumentCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        // Then
        assertAll(() -> {
            Mockito.verify(transferRepository, Mockito.times(1))
                    .findAllByTransferStatusAndTransferDateBefore(eq(SCHEDULED), localDateTimeArgumentCaptor.capture());
        });

    }

}