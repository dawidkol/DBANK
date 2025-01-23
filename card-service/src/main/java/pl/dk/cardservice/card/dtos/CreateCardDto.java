package pl.dk.cardservice.card.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;
import pl.dk.cardservice.constraint.CardExpiryConstraint;
import pl.dk.cardservice.enums.CardType;

import java.time.LocalDate;

@Builder
public record CreateCardDto(
        @NotNull
        @NotBlank
        String accountNumber,
        @NotNull
        @NotBlank
        String userId,
        @NotNull
        @CardExpiryConstraint
        int yearsValid,
        @NotNull
        @FutureOrPresent
        LocalDate activeStart,
        @NotNull
        CardType cardType
) {
}
