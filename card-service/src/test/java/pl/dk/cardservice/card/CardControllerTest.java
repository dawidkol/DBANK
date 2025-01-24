package pl.dk.cardservice.card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;
import pl.dk.cardservice.enums.CardType;
import pl.dk.cardservice.httpclient.AccountServiceFeignClient;
import pl.dk.cardservice.httpclient.UserServiceFeignClient;
import pl.dk.cardservice.httpclient.dto.AccountDto;
import pl.dk.cardservice.httpclient.dto.UserDto;

import java.time.LocalDate;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.*;

@SpringBootTest(webEnvironment = RANDOM_PORT,
        properties = {"eureka.client.enabled=false", "scheduler.cards-active=0/1 * * * * *"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CardControllerTest {

    @MockitoBean
    private UserServiceFeignClient userServiceFeignClient;
    @MockitoBean
    private AccountServiceFeignClient accountServiceFeignClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoSpyBean
    private CardScheduler cardScheduler;

    @Test
    @DisplayName("Typical scenario with Card Controller")
    void typicalScenarioWithCardController() {
        // 1. User wants to create card. Expected status code: 201 CREATED
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

        // When
        ResponseEntity<CardDto> cardDtoResponseEntity = testRestTemplate.postForEntity(
                "/cards",
                createCardDto,
                CardDto.class);

        // Then
        assertAll(() -> {
            verify(userServiceFeignClient, times(1)).getUserById(anyString());
            verify(accountServiceFeignClient, times(1)).getAccountById(anyString());
        }, () -> {
            assertEquals(CREATED, cardDtoResponseEntity.getStatusCode());
            assertNotNull(cardDtoResponseEntity.getBody());
        });

        // 2. User wants to get card by given id. Expected status code: 200 OK
        // Given
        String cardId = cardDtoResponseEntity.getBody().cardId();

        // When
        ResponseEntity<CardDto> forEntity = testRestTemplate.getForEntity(
                "/cards/{cardId}",
                CardDto.class,
                cardId);

        // Then
        assertAll(() -> {
            assertEquals(OK, forEntity.getStatusCode());
            assertNotNull(forEntity.getBody());
        });

        // 3. User wants to get all his active cards. Expected status code: 200 OK
        // Given // When
        ResponseEntity<List<CardDto>> userCardsList = testRestTemplate.exchange("/cards/{userId}/all",
                GET,
                null,
                new ParameterizedTypeReference<List<CardDto>>() {
                },
                createCardDto.userId());

        // Then
        assertAll(() -> {
            assertEquals(1, userCardsList.getBody().size());
        });

        // 4. User wants to delete his card. Expected status code 204 NO_CONTENT
        // Given // When
        ResponseEntity<Void> delete = testRestTemplate.exchange(
                "/cards/{cardId}",
                DELETE,
                null, Void.class,
                cardDtoResponseEntity.getBody().cardId());
        // Then
        assertAll(() -> {
            assertEquals(NO_CONTENT, delete.getStatusCode());
        });

    }

    @Test
    @DisplayName("It should invoke scheduler methods successfully")
    void itShouldInvokeSchedulerMethodsSuccessfully() {
        // Given // When // Then
        await().untilAsserted(() -> {
            verify(cardScheduler, atLeastOnce()).activeCards();
        });
    }
}