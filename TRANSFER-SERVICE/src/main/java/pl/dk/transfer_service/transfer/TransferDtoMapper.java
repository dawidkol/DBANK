package pl.dk.transfer_service.transfer;

import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferEvent;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

class TransferDtoMapper {

    public static Transfer map(CreateTransferDto createTransferDto) {
        String currency = createTransferDto.currencyType();
        CurrencyType currencyType = CurrencyType.valueOf(currency);
        return Transfer.builder()
                .senderAccountNumber(createTransferDto.senderAccountNumber())
                .recipientAccountNumber(createTransferDto.recipientAccountNumber())
                .amount(createTransferDto.amount())
                .currencyType(currencyType)
                .transferDate(createTransferDto.transferDate())
                .description(createTransferDto.description())
                .build();
    }

    public static TransferDto map(Transfer transfer) {
        return TransferDto.builder()
                .transferId(transfer.getId())
                .senderAccountNumber(hideAccountNumber(transfer.getSenderAccountNumber()))
                .recipientAccountNumber(hideAccountNumber(transfer.getRecipientAccountNumber()))
                .amount(transfer.getAmount())
                .currencyType(transfer.getCurrencyType().name())
                .transferDate(transfer.getTransferDate())
                .transferStatus(transfer.getTransferStatus().name())
                .description(transfer.getDescription())
                .balanceAfterTransfer(transfer.getBalanceAfterTransfer())
                .build();
    }

    private static String hideAccountNumber(String accountNumber) {
        String substring = accountNumber.substring(22);
        return "**********************" + substring;
    }

    public static TransferEvent mapToEvent(Transfer transfer) {
        return TransferEvent.builder()
                .transferId(transfer.getId())
                .senderAccountNumber(transfer.getSenderAccountNumber())
                .recipientAccountNumber(transfer.getRecipientAccountNumber())
                .amount(transfer.getAmount())
                .currencyType(transfer.getCurrencyType().name())
                .transferDate(transfer.getTransferDate())
                .transferStatus(transfer.getTransferStatus().name())
                .build();
    }
}
