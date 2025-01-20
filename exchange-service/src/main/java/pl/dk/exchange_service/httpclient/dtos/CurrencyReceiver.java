package pl.dk.exchange_service.httpclient.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import pl.dk.exchange_service.enums.CurrencyType;

@Builder
public record CurrencyReceiver(
        @JsonProperty("currency")
        String name,
        CurrencyType code,
        Rates[] rates
) {
}
