package pl.dk.exchange_service.exchange;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.dk.exchange_service.constraint.CurrencyTypeConstraint;
import pl.dk.exchange_service.enums.CurrencyType;
import pl.dk.exchange_service.exchange.dtos.CalculateResult;
import pl.dk.exchange_service.exchange.dtos.ExchangeDto;
import pl.dk.exchange_service.exchange.dtos.ExchangeResultDto;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import static pl.dk.exchange_service.constants.PagingAndSorting.*;
import static pl.dk.exchange_service.constants.PagingAndSorting.PAGE_DEFAULT;

@RestController
@RequestMapping("/exchanges")
@RequiredArgsConstructor
@Validated
class ExchangeController {

    private final ExchangeService exchangeService;

    @PostMapping
    public ResponseEntity<ExchangeResultDto> exchange(@Valid @RequestBody ExchangeDto exchangeDto) {
        ExchangeResultDto exchangeResultDto = exchangeService.exchangeCurrencies(exchangeDto);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(exchangeResultDto.exchangeId())
                .toUri();
        return ResponseEntity.created(uri).body(exchangeResultDto);
    }

    @GetMapping
    public ResponseEntity<CalculateResult> calculateExchange(@CurrencyTypeConstraint @RequestParam CurrencyType from,
                                                             @CurrencyTypeConstraint @RequestParam CurrencyType to,
                                                             BigDecimal valueFrom) {
        CalculateResult result = exchangeService.calculateExchange(from, to, valueFrom);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<ExchangeResultDto>> getAllAccountExchanges(@PathVariable String accountNumber,
                                                                          @RequestParam(required = false, defaultValue = PAGE_DEFAULT) int page,
                                                                          @RequestParam(required = false, defaultValue = SIZE_DEFAULT) int size) {
        List<ExchangeResultDto> allAccountExchanges = exchangeService.getAllAccountExchanges(accountNumber, page, size);
        return ResponseEntity.ok(allAccountExchanges);
    }
}
