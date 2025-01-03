package pl.dk.transfer_service.transfer;

import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

import java.util.List;

public interface TransferService {

    TransferDto createTransfer(CreateTransferDto createTransferDto);

    TransferDto getTransferById(String transferId);

    void updateTransferStatus(String transferId, TransferStatus transferStatus);

    List<TransferDto> getAllTransfersFromAccount(String accountNumber, int page, int size);

    List<TransferDto> getAllTransferFromTo(String senderAccountNumber, String recipientAccountNumber, int page, int size);
}
