package pl.dk.exchange_service.config;

import org.springframework.core.convert.converter.Converter;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exception.RequestParameterException;

import java.util.Arrays;

class CurrencyTypeCustomConverter implements Converter<String, CurrencyType> {

    @Override
    public CurrencyType convert(String source) {
        String upperCase = source.toUpperCase();
        try {
            return CurrencyType.valueOf(upperCase);
        } catch (IllegalArgumentException e) {
            CurrencyType[] supportedCurrencies = CurrencyType.values();
            throw new RequestParameterException("Currency [%s] not supported. Supported supportedCurrencies: [%s["
                    .formatted(upperCase, Arrays.toString(supportedCurrencies)));
        }
    }
}
