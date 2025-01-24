package pl.dk.cardservice.card;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import pl.dk.cardservice.enums.CardType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "cards")
@NoArgsConstructor
@Getter
@Setter
@ToString
@DynamicUpdate
class Card extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotNull
    @NotBlank
    private String cardNumber;
    @NotNull
    @NotBlank
    private String accountNumber;
    @NotNull
    @NotBlank
    private String userId;
    @NotNull
    @NotBlank
    private String cardHolderName;
    @NotNull
    @FutureOrPresent
    private LocalDate activeStart;
    @NotNull
    @Future
    private LocalDate expiryDate;
    @NotNull
    @NotBlank
    private String cvv;
    @NotNull
    @Enumerated(EnumType.STRING)
    private CardType cardType;
    @NotNull
    private Boolean isActive;

    @Builder
    public Card(LocalDateTime createdAt,
                String createdBy,
                LocalDateTime updatedAt,
                String updatedBy,
                String id,
                String cardNumber,
                String accountNumber,
                String userId,
                String cardHolderName,
                LocalDate activeStart,
                LocalDate expiryDate,
                String cvv,
                CardType cardType,
                Boolean isActive) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.cardNumber = cardNumber;
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.cardHolderName = cardHolderName;
        this.activeStart = activeStart;
        this.expiryDate = expiryDate;
        this.cvv = cvv;
        this.cardType = cardType;
        this.isActive = isActive;
    }
}
