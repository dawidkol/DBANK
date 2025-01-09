package pl.dk.transfer_service.transfer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.transfer_service.enums.TransferStatus;
import pl.dk.transfer_service.exception.AccountNotExistsException;
import pl.dk.transfer_service.exception.InsufficientBalanceException;
import pl.dk.transfer_service.exception.TransferNotFoundException;
import pl.dk.transfer_service.exception.TransferStatusException;
import pl.dk.transfer_service.httpClient.AccountFeignClient;
import pl.dk.transfer_service.httpClient.dtos.AccountBalanceDto;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferEvent;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static pl.dk.transfer_service.kafka.KafkaConstants.CREATE_TRANSFER_EVENT;
import static pl.dk.transfer_service.enums.TransferStatus.*;

@Slf4j
@Service
class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final AccountFeignClient accountFeignClient;
    private final KafkaTemplate<String, TransferEvent> kafkaTemplate;
    private final String transferScheduler;

    public TransferServiceImpl(TransferRepository transferRepository,
                               AccountFeignClient accountFeignClient,
                               KafkaTemplate<String, TransferEvent> kafkaTemplate,
                               @Value("${scheduler.transfer}") String transferScheduler) {
        this.transferRepository = transferRepository;
        this.accountFeignClient = accountFeignClient;
        this.kafkaTemplate = kafkaTemplate;
        this.transferScheduler = transferScheduler;
    }

    @Override
    @Transactional
    public TransferDto createTransfer(CreateTransferDto createTransferDto) {
        AccountBalanceDto accountBalanceDto = validateRequest(createTransferDto);
        Transfer transferToSave = setTransferObjectToSave(createTransferDto, accountBalanceDto);
        Transfer savedTransfer = transferRepository.save(transferToSave);
        if (savedTransfer.getTransferStatus().equals(PENDING)) {
            createEventAndSendToKafka(savedTransfer);
        }
        return TransferDtoMapper.map(savedTransfer);
    }

    private void createEventAndSendToKafka(Transfer savedTransfer) {
        TransferEvent transferEvent = TransferDtoMapper.mapToEvent(savedTransfer);
        kafkaTemplate.send(CREATE_TRANSFER_EVENT,
                transferEvent.transferId(),
                transferEvent);
    }

    private static Transfer setTransferObjectToSave(CreateTransferDto createTransferDto, AccountBalanceDto accountBalanceDto) {
        Transfer transferToSave = TransferDtoMapper.map(createTransferDto);
        LocalDate now = LocalDate.now();
        LocalDate transferLocalDate = createTransferDto.transferDate().toLocalDate();
        if (transferLocalDate.isEqual(now)) {
            transferToSave.setTransferStatus(PENDING);
        } else if (transferLocalDate.isAfter(now)) {
            transferToSave.setTransferStatus(SCHEDULED);
        }
        transferToSave.setBalanceAfterTransfer(accountBalanceDto.balance().subtract(createTransferDto.amount()));
        return transferToSave;
    }

    private AccountBalanceDto validateRequest(CreateTransferDto createTransferDto) {
        ResponseEntity<AccountDto> sender = accountFeignClient.getAccountById(createTransferDto.senderAccountNumber());
        ResponseEntity<AccountDto> recipient = accountFeignClient.getAccountById(createTransferDto.recipientAccountNumber());
        if (sender.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND) || recipient.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            throw new AccountNotExistsException("Account with id: %s not exists");
        }
        ResponseEntity<AccountBalanceDto> accountBalanceResponse = accountFeignClient.getAccountBalanceByAccountNumberAndCurrencyType(
                createTransferDto.senderAccountNumber(),
                createTransferDto.currencyType());
        if (accountBalanceResponse.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
            throw new AccountNotExistsException("Account with id: %s not exists");
        }
        AccountBalanceDto accountBalanceDto = accountBalanceResponse.getBody();
        int i = Objects.requireNonNull(accountBalanceDto).balance().compareTo(createTransferDto.amount());
        if (i < 0) {
            throw new InsufficientBalanceException("Insufficient sender account balance");
        }
        return accountBalanceDto;
    }

    @Override
    public TransferDto getTransferById(String transferId) {
        return transferRepository.findById(transferId)
                .map(TransferDtoMapper::map)
                .orElseThrow(() ->
                        new TransferNotFoundException("Transfer with id: %s not found".formatted(transferId)));
    }

    @Override
    @Transactional
    public void updateTransferStatus(String transferId, TransferStatus transferStatus) {
        transferRepository.findById(transferId)
                .ifPresentOrElse(
                        transfer -> {
                            transfer.setTransferStatus(transferStatus);
                        }, () -> {
                            throw new TransferNotFoundException("Transfer with id: %s not found".formatted(transferId));
                        }
                );
    }

    @Override
    public List<TransferDto> getAllTransfersFromAccount(String accountNumber, int page, int size) {
        return transferRepository.findAllBySenderAccountNumber(accountNumber,
                        PageRequest.of(page - 1, size))
                .stream()
                .map(TransferDtoMapper::map)
                .toList();
    }

    @Override
    public List<TransferDto> getAllTransferFromTo(String senderAccountNumber, String recipientAccountNumber, int page, int size) {
        return transferRepository.findAllBySenderAccountNumberAndRecipientAccountNumber(senderAccountNumber,
                        recipientAccountNumber,
                        PageRequest.of(page - 1, size))
                .stream()
                .map(TransferDtoMapper::map)
                .toList();
    }

    @Override
    @Scheduled(cron = "${scheduler.transfer}")
    @Async
    public void executeScheduledTransfers() {
        LocalDateTime now = LocalDateTime.now();
        log.info("Starting sending scheduled transfers to kafka");
        transferRepository.findAllByTransferStatusAndTransferDateBefore(SCHEDULED,
                        now)
                .forEach(transfer -> {
                    createEventAndSendToKafka(transfer);
                    log.info("TransferEvent with transferId: {} sent successfully to KAFKA TOPIC: {}",
                            transfer.getId(),
                            CREATE_TRANSFER_EVENT);
                });
        log.info("All scheduled transfers sent successfully to KAFKA TOPIC: {}", CREATE_TRANSFER_EVENT);
    }

    @Override
    @Transactional
    public void cancelScheduledTransfer(String transferId) {
        LocalDateTime now = LocalDateTime.now();
        transferRepository.findByIdAndTransferStatus(transferId, SCHEDULED)
                .ifPresentOrElse(transfer -> {
                    LocalDateTime transferDate = transfer.getTransferDate();
                    CronExpression cronExpression = CronExpression.parse(transferScheduler);
                    LocalDateTime next = cronExpression.next(transferDate);
                    if (Objects.requireNonNull(next).isAfter(now)) {
                        transfer.setTransferStatus(CANCELLED);
                    } else {
                        throw new TransferStatusException("Current date: %s is after scheduled transfer date: %s"
                                .formatted(now, transferDate));
                    }
                    log.info("Transfer with id: {} CANCELLED", transfer.getId());
                }, () -> {
                    throw new TransferNotFoundException("Transfer with id: %s and status: %s not found"
                            .formatted(transferId, SCHEDULED.name()));
                });
    }
}