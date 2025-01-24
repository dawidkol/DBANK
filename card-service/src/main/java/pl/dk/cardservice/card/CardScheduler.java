package pl.dk.cardservice.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
class CardScheduler {

    private final CardRepository cardRepository;

    @Transactional
    @Scheduled(cron = "${scheduler.cards-active}")
    public void activeCards() {
        log.info("Starting setting cards as active");
        List<Card> list = cardRepository.findAllByIsActiveAndActiveStartLessThanEqual(false, LocalDate.now())
                .stream()
                .peek(card -> card.setIsActive(true))
                .toList();
        log.info("{} cards set as active", list.size());
    }
}
