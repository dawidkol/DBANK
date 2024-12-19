package pl.dk.transfer_service.transfer;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfers")
@Getter
@Setter
@NoArgsConstructor
class Transfer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String senderAccountNumber;
    private String recipientAccountNumber;
    private BigDecimal amount;
    @Enumerated(value = EnumType.STRING)
    private CurrencyType currencyType;
    private LocalDateTime transferDate;
    @Enumerated(value = EnumType.STRING)
    private TransferStatus transferStatus;
    private String description;
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
