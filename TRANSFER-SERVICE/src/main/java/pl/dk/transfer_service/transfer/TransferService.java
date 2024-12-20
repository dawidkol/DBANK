package pl.dk.transfer_service.transfer;

import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

interface TransferService {

    TransferDto createTransfer(CreateTransferDto createTransferDto);
}
