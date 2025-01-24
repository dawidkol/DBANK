package pl.dk.cardservice.card;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.dk.cardservice.card.dtos.CardDto;
import pl.dk.cardservice.card.dtos.CreateCardDto;
import pl.dk.cardservice.exception.FeignClientException;
import pl.dk.cardservice.httpclient.AccountServiceFeignClient;
import pl.dk.cardservice.httpclient.UserServiceFeignClient;
import pl.dk.cardservice.httpclient.dto.AccountDto;
import pl.dk.cardservice.httpclient.dto.UserDto;

import java.time.LocalDate;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserServiceFeignClient userServiceFeignClient;
    private final AccountServiceFeignClient accountServiceFeignClient;
    private final CardDataGenerator cardDataGenerator;

    @Override
    @Transactional
    public CardDto createCard(CreateCardDto createCardDto) {
        UserDto userDto = validateRequestAndGetUser(createCardDto);

        Card cardToSave = Card.builder()
                .cardNumber(cardDataGenerator.generateCardNumber())
                .accountNumber(createCardDto.accountNumber())
                .userId(userDto.userId())
                .cardHolderName(userDto.firstName() + " " + userDto.lastName())
                .activeFrom(createCardDto.activeStart())
                .expiryDate(createCardDto.activeStart().plusYears(createCardDto.yearsValid()))
                .cvv(cardDataGenerator.generateCvv())
                .cardType(createCardDto.cardType())
                .isActive(!createCardDto.activeStart().isAfter(LocalDate.now()))
                .build();

        Card savedCard = cardRepository.save(cardToSave);
        return CardDtoMapper.map(savedCard);
    }

    private UserDto validateRequestAndGetUser(CreateCardDto createCardDto) {
        ResponseEntity<UserDto> userById = userServiceFeignClient.getUserById(createCardDto.userId());
        if (userById.getStatusCode().isSameCodeAs(NOT_FOUND)) {
            throw new FeignClientException("User with id %s not found".formatted(createCardDto.userId()));
        }
        ResponseEntity<AccountDto> accountById = accountServiceFeignClient.getAccountById(createCardDto.accountNumber());
        if (accountById.getStatusCode().isSameCodeAs(NOT_FOUND)) {
            throw new FeignClientException("Account with accountNumber %s not found".formatted(createCardDto.accountNumber()));
        }
        UserDto userDto = userById.getBody();
        String userIdFromUserService = userDto.userId();
        String userIdFromAccountService = accountById.getBody().userId();
        String accountNumberFromAccountService = accountById.getBody().accountNumber();
        if (!userIdFromAccountService.equals(userIdFromUserService)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with id: [%s] is not owner of the account: [%s] "
                    .formatted(userIdFromUserService, accountNumberFromAccountService));
        }
        return userDto;
    }
}
