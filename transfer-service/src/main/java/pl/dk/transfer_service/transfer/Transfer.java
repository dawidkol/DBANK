package pl.dk.transfer_service.transfer;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.dk.transfer_service.enums.CurrencyType;
import pl.dk.transfer_service.enums.TransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
public class Transfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Pattern(regexp = "\\d{26}")
    private String senderAccountNumber;
    @Pattern(regexp = "\\d{26}")
    private String recipientAccountNumber;
    @NotNull
    @Positive
    private BigDecimal amount;
    @Enumerated(value = EnumType.STRING)
    @NotNull
    private CurrencyType currencyType;
    @FutureOrPresent
    private LocalDateTime transferDate;
    @Enumerated(value = EnumType.STRING)
    private TransferStatus transferStatus;
    @NotBlank
    @Size(min = 5, max = 300)
    private String description;
    @NotNull
    private BigDecimal balanceAfterTransfer;

    @Builder
    public Transfer(LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy, String id,
                    String senderAccountNumber, String recipientAccountNumber, BigDecimal amount,
                    CurrencyType currencyType, LocalDateTime transferDate, TransferStatus transferStatus,
                    String description, BigDecimal balanceAfterTransfer) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.senderAccountNumber = senderAccountNumber;
        this.recipientAccountNumber = recipientAccountNumber;
        this.amount = amount;
        this.currencyType = currencyType;
        this.transferDate = transferDate;
        this.transferStatus = transferStatus;
        this.description = description;
        this.balanceAfterTransfer = balanceAfterTransfer;
    }
}
