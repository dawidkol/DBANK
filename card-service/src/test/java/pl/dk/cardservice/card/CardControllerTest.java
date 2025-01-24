package pl.dk.cardservice.card;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;
import pl.dk.cardservice.enums.CardType;
import pl.dk.cardservice.httpclient.AccountServiceFeignClient;
import pl.dk.cardservice.httpclient.UserServiceFeignClient;
import pl.dk.cardservice.httpclient.dto.AccountDto;
import pl.dk.cardservice.httpclient.dto.UserDto;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.*;

@SpringBootTest(webEnvironment = RANDOM_PORT,
        properties = "eureka.client.enabled=false")
class CardControllerTest {

    @MockitoBean
    private UserServiceFeignClient userServiceFeignClient;
    @MockitoBean
    private AccountServiceFeignClient accountServiceFeignClient;

    @Autowired
    private TestRestTemplate testRestTemplate;

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
        ResponseEntity<CardDto> cardDtoResponseEntity = testRestTemplate.postForEntity("/cards", createCardDto, CardDto.class);

        // Then
        assertAll(() -> {
            verify(userServiceFeignClient, times(1)).getUserById(anyString());
            verify(accountServiceFeignClient, times(1)).getAccountById(anyString());
        }, () -> {
            assertEquals(HttpStatus.CREATED, cardDtoResponseEntity.getStatusCode());
            assertNotNull(cardDtoResponseEntity.getBody());
        });
    }
}