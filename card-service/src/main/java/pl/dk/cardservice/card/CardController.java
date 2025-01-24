package pl.dk.cardservice.card;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;
import pl.dk.cardservice.constants.PagingAndSorting;

import java.net.URI;
import java.util.List;

import static pl.dk.cardservice.constants.CardConstants.ACTIVE_CARD;
import static pl.dk.cardservice.constants.PagingAndSorting.*;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
@Validated
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

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDto> getCardById(@NotBlank @PathVariable String cardId) {
        CardDto cardById = cardService.getCardById(cardId);
        return ResponseEntity.ok(cardById);
    }

    @GetMapping("/{userId}/all")
    public ResponseEntity<List<CardDto>> getAllUserCards(@NotBlank @PathVariable String userId,
                                                         @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
                                                         @RequestParam(required = false, defaultValue = SIZE_DEFAULT) int size,
                                                         @RequestParam(required = false, defaultValue = ACTIVE_CARD) Boolean isActive) {
        List<CardDto> allUserCards = cardService.getAllUserCards(userId, page, size, isActive);
        return ResponseEntity.ok(allUserCards);
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<?> deleteCardById(@NotBlank @PathVariable String cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

}
