package pl.dk.exchange_service.exchange;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import pl.dk.exchange_service.constraint.CurrencyTypeConstraint;
import pl.dk.exchange_service.currency.BaseEntity;
import pl.dk.exchange_service.enums.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchanges")
@NoArgsConstructor
@Getter
@Setter
@ToString
@DynamicUpdate
class Exchange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotBlank
    @NotBlank
    private String accountNumber;
    @NotNull
    @CurrencyTypeConstraint
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyFrom;
    @NotNull
    @Positive
    private BigDecimal valueFrom;
    @NotNull
    @CurrencyTypeConstraint
    @Enumerated(EnumType.STRING)
    private CurrencyType currencyTo;
    @NotNull
    @Positive
    private BigDecimal rate;
    @NotNull
    @Positive
    private BigDecimal result;

    @Builder
    public Exchange(LocalDateTime createdAt,
                    String createdBy,
                    LocalDateTime updatedAt,
                    String updatedBy,
                    String id,
                    String accountNumber,
                    CurrencyType currencyFrom,
                    BigDecimal valueFrom,
                    CurrencyType currencyTo,
                    BigDecimal rate,
                    BigDecimal result) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.accountNumber = accountNumber;
        this.currencyFrom = currencyFrom;
        this.valueFrom = valueFrom;
        this.currencyTo = currencyTo;
        this.rate = rate;
        this.result = result;
    }
}
