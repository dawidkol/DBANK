package pl.dk.exchange_service.currency;

import com.github.fge.jsonpatch.mergepatch.JsonMergePatch;
import pl.dk.exchange_service.currency.dtos.CurrencyDto;

import java.util.List;

interface CurrencyService {

    CurrencyDto save(Currency currency);

    CurrencyDto getCurrencyById(String currencyId);

    List<CurrencyDto> getAllCurrencies(int page, int size);

    void updateCurrency(String currencyId, JsonMergePatch jsonMergePatch);

    void deleteCurrencyById(String currencyId);
}
