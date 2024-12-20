package pl.dk.transfer_service.transfer;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.transfer_service.exception.InsufficientBalanceException;
import pl.dk.transfer_service.httpClient.AccountFeignClient;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

@Service
@RequiredArgsConstructor
class TransferServiceImpl implements TransferService {

    private final TransferRepository transferRepository;
    private final AccountFeignClient accountFeignClient;

    @Override
    @Transactional
    public TransferDto createTransfer(CreateTransferDto createTransferDto) {
        AccountDto senderAccountDto = validateRequest(createTransferDto);
        Transfer transferToSave = setTransferObjectToSave(createTransferDto, senderAccountDto);
        Transfer savedTransfer = transferRepository.save(transferToSave);
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
        AccountDto senderAccountDto = sender.getBody();
        assert senderAccountDto != null;
        int i = senderAccountDto.balance().compareTo(createTransferDto.amount());
        if (i < 0) {
            throw new InsufficientBalanceException("Insufficient sender account balance");
        }
        return senderAccountDto;
    }
}
