package pl.dk.exchange_service.httpclient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import pl.dk.exchange_service.currency.Currency;
import pl.dk.exchange_service.currency.CurrencyRepository;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.httpclient.dtos.CurrencyReceiver;
import pl.dk.exchange_service.httpclient.dtos.Rates;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
@Slf4j
class CurrencyHttpClient {
    public static final String BASE_URL = "https://api.nbp.pl/api";

    private final RestClient restClient;
    private final CurrencyRepository currencyRepository;

    public CurrencyHttpClient(RestClient.Builder restClient, CurrencyRepository currencyRepository) {
        this.restClient = restClient
                .baseUrl(BASE_URL)
                .defaultHeader(APPLICATION_JSON_VALUE)
                .build();
        this.currencyRepository = currencyRepository;
    }

    @Transactional
    public void fetchCurrencies() {
        List<Currency> currenciesToSave = new ArrayList<>();
        for (CurrencyType currencyCode : CurrencyType.values()) {
            if (currencyCode.equals(CurrencyType.PLN))
                continue;

            String uri = ("/exchangerates/rates/c/{currencyType}");
            CurrencyReceiver currency = restClient.get()
                    .uri(uri, currencyCode.toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<CurrencyReceiver>() {
                    });

            if (currency != null) {
                Rates rate = currency.rates()[0];
                currencyRepository.findAllByCurrencyType(currencyCode)
                        .ifPresentOrElse(existingCurrency -> {
                            updateCurrency(existingCurrency, rate);
                        }, () -> {
                            Currency currencyToSave = createCurrency(currency, rate);
                            currenciesToSave.add(currencyToSave);
                        });
            }
        }
        if (!currenciesToSave.isEmpty()) {
            currencyRepository.saveAll(currenciesToSave);
        }
    }

    private Currency createCurrency(CurrencyReceiver currency, Rates rate) {
        return Currency.builder()
                .name(currency.name())
                .currencyType(currency.code())
                .effectiveDate(rate.effectiveDate())
                .bid(rate.bid().setScale(2, RoundingMode.HALF_DOWN))
                .ask(rate.ask().setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private void updateCurrency(Currency existingCurrency, Rates rate) {
        existingCurrency.setEffectiveDate(rate.effectiveDate());
        existingCurrency.setBid(rate.bid().setScale(2, RoundingMode.HALF_DOWN));
        existingCurrency.setAsk(rate.ask().setScale(2, RoundingMode.HALF_UP));
    }
}
