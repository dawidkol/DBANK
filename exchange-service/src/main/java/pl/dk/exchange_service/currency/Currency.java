package pl.dk.exchange_service.currency;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.dk.exchange_service.enums.CurrencyType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "currencies")
@NoArgsConstructor
@Getter
@Setter
class Currency extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotNull
    @NotBlank
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    @NotNull
    private CurrencyType currencyType;
    private LocalDate effectiveDate;
    @NotNull
    private BigDecimal bid;
    @NotNull
    private BigDecimal ask;

    @Builder
    public Currency(LocalDateTime createdAt,
                    String createdBy,
                    LocalDateTime updatedAt,
                    String updatedBy,
                    String id,
                    String name,
                    CurrencyType currencyType,
                    LocalDate effectiveDate,
                    BigDecimal bid,
                    BigDecimal ask) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.name = name;
        this.currencyType = currencyType;
        this.effectiveDate = effectiveDate;
        this.bid = bid;
        this.ask = ask;
    }
}
