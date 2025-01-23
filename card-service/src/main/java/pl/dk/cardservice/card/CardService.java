package pl.dk.cardservice.card;

import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;

interface CardService {

    CardDto createCard(CreateCardDto createCardDto);
}
