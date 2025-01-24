package pl.dk.cardservice.card;

import pl.dk.cardservice.card.dtos.CardDto;

class CardDtoMapper {

    public static CardDto map(Card card) {
        return CardDto.builder()
                .cardId(card.getId())
                .cardNumber(card.getCardNumber())
                .accountNumber(hideAccountNumber(card.getAccountNumber()))
                .userId(hideUuid(card.getUserId()))
                .cardHolderName(hideCardHolder(card.getCardHolderName()))
                .activeFrom(card.getActiveStart())
                .expiryDate(card.getExpiryDate())
                .cvv(hideCvv(card.getCvv()))
                .cardType(card.getCardType())
                .isActive(card.getIsActive())
                .build();
    }

    private static String hideUuid(String uuid) {
        String end = uuid.substring(32);
        String start = uuid.substring(1, 5);
        StringBuilder middle = new StringBuilder();
        for (int i = 1; i <= 28; i++) {
            middle.append("*");
            i++;
        }
        return start + middle + end;
    }

    private static String hideAccountNumber(String accountNumber) {
        String substring = accountNumber.substring(22);
        return "**********************" + substring;
    }

    private static String hideCardHolder(String cardHolder) {
        String[] s = cardHolder.split(" ");
        return s[0] + s[1].charAt(0);
    }

    private static String hideCvv(String cvv) {
        return "***";
    }
}
