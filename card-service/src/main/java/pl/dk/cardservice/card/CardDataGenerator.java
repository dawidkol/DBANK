package pl.dk.cardservice.card;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@Slf4j
class CardDataGenerator {

    private static final int CARD_DIGITS = 16;
    private static final int CVV_DIGITS = 3;

    private final Random random;
    private final CardRepository cardRepository;

    public CardDataGenerator(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
        this.random = new Random(System.currentTimeMillis());
    }

    public String generateCardNumber() {
        String result;
        do {
            StringBuilder cardNumberBuilder = buildNumber(CARD_DIGITS);
            result = cardNumberBuilder.toString();
        } while (cardRepository.findFirstByCardNumber(result).isPresent());
        return result;
    }

    public String generateCvv() {
        StringBuilder cvvBuilder = buildNumber(CVV_DIGITS);
        return cvvBuilder.toString();
    }

    private StringBuilder buildNumber(int digits) {
        StringBuilder cardNumberBuilder = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            cardNumberBuilder.append(String.valueOf(random.nextInt(10)));
        }
        return cardNumberBuilder;
    }
}

