package pl.dk.cardservice.card;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;
import pl.dk.cardservice.enums.CardType;
import pl.dk.cardservice.httpclient.AccountServiceFeignClient;
import pl.dk.cardservice.httpclient.UserServiceFeignClient;
import pl.dk.cardservice.httpclient.dto.AccountDto;
import pl.dk.cardservice.httpclient.dto.UserDto;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CardServiceTest {

    @Mock
    private CardRepository cardRepository;
    @Mock
    private UserServiceFeignClient userServiceFeignClient;
    @Mock
    private AccountServiceFeignClient accountServiceFeignClient;

    private AutoCloseable autoCloseable;

    private CardService underTest;

    @BeforeEach
    void setUp() {
        autoCloseable = MockitoAnnotations.openMocks(this);
        CardDataGenerator cardDataGenerator = new CardDataGenerator(cardRepository);
        underTest = new CardServiceImpl(cardRepository, userServiceFeignClient, accountServiceFeignClient, cardDataGenerator);
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should save card successfully")
    void itSaveCardSuccessfully() {
        // Given
        CreateCardDto createCardDto = CreateCardDto.builder()
                .accountNumber("63113996792356739067805147")
                .userId("63d520d6-df76-4ed7-a8a6-2f597248cfb1")
                .yearsValid(5)
                .activeStart(LocalDate.now())
                .cardType(CardType.CREDIT)
                .build();

        when(userServiceFeignClient.getUserById(createCardDto.userId()))
                .thenReturn(ResponseEntity.ok(UserDto.builder().userId(createCardDto.userId()).build()));
        when(accountServiceFeignClient.getAccountById(createCardDto.accountNumber()))
                .thenReturn(ResponseEntity.ok(AccountDto.builder().accountNumber(createCardDto.accountNumber())
                        .userId(createCardDto.userId())
                        .build()));
        when(cardRepository.findFirstByCardNumber(anyString())).thenReturn(Optional.empty());


        Card card = Card.builder()
                .id(UUID.randomUUID().toString())
                .cardNumber("0000000000000000")
                .accountNumber(createCardDto.accountNumber())
                .userId(createCardDto.userId())
                .cardHolderName("John Doe")
                .activeFrom(createCardDto.activeStart())
                .expiryDate(createCardDto.activeStart().plusYears(createCardDto.yearsValid()))
                .cvv("123")
                .cardType(CardType.CREDIT)
                .isActive(true)
                .build();

        when(cardRepository.save(any(Card.class)))
                .thenReturn(card);

        // When
        CardDto result = underTest.createCard(createCardDto);

        // Then
        assertAll(() -> {
            verify(userServiceFeignClient, times(1)).getUserById(anyString());
            verify(accountServiceFeignClient, times(1)).getAccountById(anyString());
            verify(cardRepository, times(1)).findFirstByCardNumber(anyString());
            verify(cardRepository, times(1)).save(any(Card.class));
        }, () -> {
            assertEquals(card.getId(), result.cardId());
            assertEquals(card.getCardNumber(), result.cardNumber());
            assertNotNull(card.getAccountNumber());
            assertNotNull(card.getUserId());
            assertNotNull(card.getCardHolderName());
            assertEquals(card.getActiveFrom(), result.activeFrom());
            assertEquals(card.getExpiryDate(), result.expiryDate());
            assertNotNull(card.getCvv());
            assertEquals(card.getCardType(), result.cardType());
            assertEquals(true, result.isActive());
        });
    }
}