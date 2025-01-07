package pl.dk.transfer_service.transfer;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.transfer_service.constants.TransferServiceConstants;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

import java.net.URI;
import java.util.List;

import static pl.dk.transfer_service.constants.TransferServiceConstants.*;
import static pl.dk.transfer_service.constants.TransferServiceConstants.SIZE_DEFAULT;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
@Validated
class TransferController {

    private final TransferService transferService;

    @PostMapping
    public ResponseEntity<TransferDto> createTransfer(@Valid @RequestBody CreateTransferDto createTransferDto) {
        TransferDto transfer = transferService.createTransfer(createTransferDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(transfer.transferId())
                .toUri();
        return ResponseEntity.created(uri).body(transfer);
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<TransferDto> getTransferById(@PathVariable String transferId) {
        TransferDto transferById = transferService.getTransferById(transferId);
        return ResponseEntity.ok(transferById);
    }

    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<List<TransferDto>> getAllTransfersFromAccount(
            @Pattern(regexp = "\\d{26}") @PathVariable String accountNumber,
            @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
            @RequestParam(required = false, defaultValue = SIZE_DEFAULT) int size) {
        List<TransferDto> allTransfersFromAccount = transferService.getAllTransfersFromAccount(accountNumber, page, size);
        return ResponseEntity.ok(allTransfersFromAccount);
    }

    @GetMapping
    public ResponseEntity<List<TransferDto>> getAllTransfersFromTo(
            @Pattern(regexp = "\\d{26}") @RequestParam String senderAccountNumber,
            @Pattern(regexp = "\\d{26}") @RequestParam String recipientAccountNumber,
            @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
            @RequestParam(required = false, defaultValue = SIZE_DEFAULT) int size) {
        List<TransferDto> allTransferFromTo = transferService.getAllTransferFromTo(
                senderAccountNumber,
                recipientAccountNumber,
                page, size);
        return ResponseEntity.ok(allTransferFromTo);
    }

}
