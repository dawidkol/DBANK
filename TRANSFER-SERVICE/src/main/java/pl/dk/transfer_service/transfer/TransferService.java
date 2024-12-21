package pl.dk.transfer_service.transfer;

import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

public interface TransferService {

    TransferDto createTransfer(CreateTransferDto createTransferDto);
    TransferDto getTransferById(String transferId);
    void updateTransferStatus(String transferId, TransferStatus transferStatus);
}
