package pl.dk.cardservice.card.dtos;

import lombok.Builder;
import pl.dk.cardservice.enums.CardType;

import java.time.LocalDate;

@Builder
public record CardDto(
        String cardId,
        String cardNumber,
        String accountNumber,
        String userId,
        String cardHolderName,
        LocalDate activeFrom,
        LocalDate expiryDate,
        String cvv,
        CardType cardType,
        Boolean isActive
) {
}
