package pl.dk.exchange_service.httpclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class CurrencyScheduler {

    private final CurrencyHttpClient currencyHttpClient;

    @Scheduled(cron = "${scheduler.update-currencies}")
    public void fetchCurrencies() {
        log.info("Starting fetching currencies from NBP");
        currencyHttpClient.fetchCurrencies();
        log.info("All currencies saved successfully");
    }
}
