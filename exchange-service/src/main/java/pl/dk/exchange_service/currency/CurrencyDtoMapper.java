package pl.dk.exchange_service.currency;

import pl.dk.exchange_service.currency.dtos.CurrencyDto;

class CurrencyDtoMapper {

    public static CurrencyDto map(Currency currency) {
        return CurrencyDto.builder()
                .currencyId(currency.getId())
                .name(currency.getName())
                .currencyType(currency.getCurrencyType())
                .effectiveDate(currency.getEffectiveDate())
                .ask(currency.getAsk())
                .bid(currency.getBid())
                .build();
    }

    public static Currency map(CurrencyDto currencyDto) {
        return Currency.builder()
                .id(currencyDto.currencyId())
                .name(currencyDto.name())
                .currencyType(currencyDto.currencyType())
                .effectiveDate(currencyDto.effectiveDate())
                .ask(currencyDto.ask())
                .bid(currencyDto.bid())
                .build();
    }
}
