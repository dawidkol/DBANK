package pl.dk.loanservice.loan;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.validator.constraints.UUID;
import pl.dk.loanservice.enums.CurrencyType;
import pl.dk.loanservice.enums.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loans")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Loan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotBlank
    @UUID
    private String userId;
    @Positive
    @NotNull
    private BigDecimal amount;
    @Positive
    @NotNull
    private BigDecimal interestRate;
    @FutureOrPresent
    private LocalDate startDate;
    @Future
    private LocalDate endDate;
    @Positive
    private Integer numberOfInstallments;
    @PositiveOrZero
    @NotNull
    private BigDecimal remainingAmount;
    @Enumerated(EnumType.STRING)
    @NotNull
    private LoanStatus status;
    @NotBlank
    @Size(min = 10, max = 300)
    private String description;
    @NotNull
    @Enumerated(value = EnumType.STRING)
    private CurrencyType currencyType;

    @Builder

    public Loan(LocalDateTime createdAt, String createdBy, LocalDateTime updatedAt, String updatedBy, String id, String userId, BigDecimal amount, BigDecimal interestRate, LocalDate startDate, LocalDate endDate, Integer numberOfInstallments, BigDecimal remainingAmount, LoanStatus status, String description, CurrencyType currencyType) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.interestRate = interestRate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfInstallments = numberOfInstallments;
        this.remainingAmount = remainingAmount;
        this.status = status;
        this.description = description;
        this.currencyType = currencyType;
    }
}
