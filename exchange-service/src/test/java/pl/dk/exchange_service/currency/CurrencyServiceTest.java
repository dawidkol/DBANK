package pl.dk.exchange_service.currency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatchException;
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.dk.exchange_service.currency.dtos.CurrencyDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.dk.exchange_service.constants.PagingAndSorting.PAGE_DEFAULT;
import static pl.dk.exchange_service.constants.PagingAndSorting.SIZE_DEFAULT;
import static pl.dk.exchange_service.enums.CurrencyType.PLN;
import static pl.dk.exchange_service.enums.CurrencyType.USD;

class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;
    private AutoCloseable autoCloseable;
    private ObjectMapper objectMapper;
    private CurrencyService underTest;

    private Currency usdCurrency;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        autoCloseable = MockitoAnnotations.openMocks(this);
        underTest = new CurrencyServiceImpl(currencyRepository, objectMapper);
        usdCurrency = Currency.builder()
                .id(UUID.randomUUID().toString())
                .name("US Dollar")
                .currencyType(USD)
                .effectiveDate(LocalDate.now())
                .bid(BigDecimal.valueOf(3.5))
                .ask(BigDecimal.valueOf(3.7))
                .build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    @DisplayName("It should save currency successfully")
    void itShouldSaveCurrencySuccessfully() {
        // Given
        when(currencyRepository.save(any())).thenReturn(usdCurrency);

        // When
        CurrencyDto result = underTest.save(any());

        // Then
        assertAll(() -> {
            verify(currencyRepository, times(1)).save(any());
        }, () -> {
            assertNotNull(result.currencyId());
        });
    }

    @Test
    @DisplayName("It should return Currency by given id")
    void itShouldReturnCurrencyByGivenId() {
        // Given
        String currencyId = usdCurrency.getId();
        when(currencyRepository.findById(any()))
                .thenReturn(Optional.of(usdCurrency));

        // When
        CurrencyDto result = underTest.getCurrencyById(currencyId);

        // Then
        assertAll(() -> {
            verify(currencyRepository, times(1))
                    .findById(currencyId);
        }, () -> {
            assertEquals(usdCurrency.getId(), result.currencyId());
            assertEquals(usdCurrency.getName(), result.name());
            assertEquals(usdCurrency.getEffectiveDate(), result.effectiveDate());
            assertEquals(usdCurrency.getBid(), result.bid());
            assertEquals(usdCurrency.getAsk(), result.ask());
            assertEquals(usdCurrency.getCurrencyType(), result.currencyType());

        });
    }

    @Test
    @DisplayName("It should return all available currencies")
    void itShouldReturnAllCurrencies() {
        // Given
        int page = Integer.parseInt(PAGE_DEFAULT);
        int size = Integer.parseInt(SIZE_DEFAULT);
        Currency plnCurrency = Currency.builder()
                .id(UUID.randomUUID().toString())
                .name("Polski złoty")
                .currencyType(PLN)
                .effectiveDate(LocalDate.of(2024, 1, 1))
                .bid(BigDecimal.valueOf(3.5))
                .ask(BigDecimal.valueOf(3.7))
                .build();

        List<Currency> currencies = List.of(usdCurrency, plnCurrency);
        when(currencyRepository.findAll(PageRequest.of(page - 1, size)))
                .thenReturn(new PageImpl<>(currencies));

        // When
        List<CurrencyDto> result = underTest.getAllCurrencies(page, size);

        // Then
        assertAll(() -> {
            assertEquals(currencies.size(), result.size());
        });
    }

    @Test
    @DisplayName("It should delete currency successfully")
    void itShouldDeleteCurrencySuccessfully() {
        // Given
        String currencyId = usdCurrency.getId();
        when(currencyRepository.findById(any()))
                .thenReturn(Optional.of(usdCurrency));

        // When
        underTest.deleteCurrencyById(currencyId);

        // Then
        assertAll(() -> {
            verify(currencyRepository, times(1)).findById(currencyId);
            verify(currencyRepository, times(1)).delete(any());
        });
    }

    @Test
    @DisplayName("It should update currency successfully")
    void itShouldUpdateCurrencySuccessfully() throws JsonPatchException, JsonProcessingException {
        // Given
        String currencyId = usdCurrency.getId();
        when(currencyRepository.findById(any()))
                .thenReturn(Optional.of(usdCurrency));

        String newName = "newName";
        String json = """
                {
                    "currencyId": "%s",
                    "name": "%s"
                }
                """.formatted(currencyId, newName);
        JsonMergePatch jsonMergePatch = objectMapper.readValue(json, JsonMergePatch.class);

        // When
        underTest.updateCurrency(currencyId, jsonMergePatch);
        ArgumentCaptor<Currency> currencyArgumentCaptor = ArgumentCaptor.forClass(Currency.class);

        // Then
        assertAll(() -> {
            verify(currencyRepository, times(1)).save(currencyArgumentCaptor.capture());
            verify(currencyRepository, times(1)).findById(currencyId);
        }, () -> {
            assertEquals(newName, currencyArgumentCaptor.getValue().getName());
        });
    }
}