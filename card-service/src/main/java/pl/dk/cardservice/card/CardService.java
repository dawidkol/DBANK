package pl.dk.cardservice.card;

import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;

import java.util.List;

interface CardService {

    CardDto createCard(CreateCardDto createCardDto);

    CardDto getCardById(String cardId);

    void deleteCard(String cardId);

    List<CardDto> getAllUserCards(String userId, int page, int size, Boolean isActive);
}
