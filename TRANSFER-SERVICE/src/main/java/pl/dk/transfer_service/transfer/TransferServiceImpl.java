package pl.dk.transfer_service.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.transfer_service.exception.AccountNotExistsException;
import pl.dk.transfer_service.exception.InsufficientBalanceException;
import pl.dk.transfer_service.exception.TransferNotFoundException;
import pl.dk.transfer_service.httpClient.AccountFeignClient;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;
import pl.dk.transfer_service.kafka.KafkaConstants;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferEvent;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

@Slf4j
@Service
@RequiredArgsConstructor
class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final AccountFeignClient accountFeignClient;
    private final KafkaTemplate<String, TransferEvent> kafkaTemplate;

    @Override
    @Transactional
    public TransferDto createTransfer(CreateTransferDto createTransferDto) {
        AccountDto senderAccountDto = validateRequest(createTransferDto);
        Transfer transferToSave = setTransferObjectToSave(createTransferDto, senderAccountDto);
        Transfer savedTransfer = transferRepository.save(transferToSave);
        TransferEvent transferEvent = TransferDtoMapper.mapToEvent(savedTransfer);
        kafkaTemplate.send(KafkaConstants.CREATE_TRANSFER_EVENT, transferEvent.transferId(), transferEvent);
        return TransferDtoMapper.map(savedTransfer);
    }

    private static Transfer setTransferObjectToSave(CreateTransferDto createTransferDto, AccountDto senderAccountDto) {
        Transfer transferToSave = TransferDtoMapper.map(createTransferDto);
        transferToSave.setTransferStatus(TransferStatus.PENDING);
        transferToSave.setBalanceAfterTransfer(senderAccountDto.balance().subtract(createTransferDto.amount()));
        return transferToSave;
    }

    private AccountDto validateRequest(CreateTransferDto createTransferDto) {
        ResponseEntity<AccountDto> sender = accountFeignClient.getAccountById(createTransferDto.senderAccountNumber());
        ResponseEntity<AccountDto> recipient = accountFeignClient.getAccountById(createTransferDto.recipientAccountNumber());
        if (sender.getStatusCode() == HttpStatus.NOT_FOUND || recipient.getStatusCode() == HttpStatus.NOT_FOUND) {
            throw new AccountNotExistsException("Account with id: %s not exists");
        }
        AccountDto senderAccountDto = sender.getBody();
        assert senderAccountDto != null;
        int i = senderAccountDto.balance().compareTo(createTransferDto.amount());
        if (i < 0) {
            throw new InsufficientBalanceException("Insufficient sender account balance");
        }
        return senderAccountDto;
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
}
