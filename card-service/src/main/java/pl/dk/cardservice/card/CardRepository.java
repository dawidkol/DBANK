package pl.dk.cardservice.card;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dk.cardservice.card.dtos.CardNumber;

import java.util.Optional;

@Repository
interface CardRepository extends JpaRepository<Card, String> {

    Optional<CardNumber> findFirstByCardNumber(String cardNumber);

}
