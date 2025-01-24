package pl.dk.cardservice.card;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;

import java.net.URI;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
class CardController {

    private final CardService cardService;

    @PostMapping
    public ResponseEntity<CardDto> createCard(@Valid @RequestBody CreateCardDto createCardDto) {
        CardDto cardDto = cardService.createCard(createCardDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{cardId}")
                .buildAndExpand(cardDto.cardId())
                .toUri();
        return ResponseEntity.created(uri).body(cardDto);
    }
}
