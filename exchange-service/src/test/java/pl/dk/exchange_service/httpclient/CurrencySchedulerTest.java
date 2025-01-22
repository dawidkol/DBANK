package pl.dk.exchange_service.httpclient;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@SpringBootTest(properties = {"eureka.client.enabled=false",
        "scheduler.update-currencies=0/1 * * * * *"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CurrencySchedulerTest {

    @MockitoSpyBean
    private CurrencyScheduler underTest;

    @Test
    @DisplayName("It should invoke currency scheduler methods")
    void itShouldInvokeCurrencySchedulerMethods() {
        // Given // When // Then
        await().atMost(1, SECONDS)
                .untilAsserted(() -> {
                    verify(underTest, atLeastOnce()).fetchCurrencies();
                });
    }
}