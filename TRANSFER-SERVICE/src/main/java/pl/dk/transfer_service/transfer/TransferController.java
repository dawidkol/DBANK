package pl.dk.transfer_service.transfer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

import java.net.URI;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
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

}
