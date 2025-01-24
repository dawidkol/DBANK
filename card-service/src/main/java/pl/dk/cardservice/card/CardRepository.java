package pl.dk.cardservice.card;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dk.cardservice.card.dtos.CardNumber;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
interface CardRepository extends JpaRepository<Card, String> {

    Optional<CardNumber> findFirstByCardNumber(String cardNumber);

    List<Card> findAllByIsActiveAndActiveStartLessThanEqual(Boolean isActive, LocalDate activeStartIsLessThan);

    Page<Card> findAllByUserIdAndIsActive(String userId, Boolean isActive, Pageable pageable);

    Optional<Card> findByIdAndIsActive(String id, Boolean isActive);

}
