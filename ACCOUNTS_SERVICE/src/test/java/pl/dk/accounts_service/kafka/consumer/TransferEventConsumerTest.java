package pl.dk.accounts_service.kafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.dk.accounts_service.account.AccountService;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.exception.AccountNotExistsException;
import pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static pl.dk.accounts_service.kafka.KafkaConstants.CREATE_TRANSFER_EVENT;

@SpringBootTest
@EmbeddedKafka(topics = CREATE_TRANSFER_EVENT)
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class TransferEventConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, TransferEvent> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @MockitoSpyBean
    private TransferEventConsumer transferEventConsumer;

    @MockitoBean
    private AccountService accountService;

    TransferEvent transferEvent;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
        transferEvent = TransferEvent.builder()
                .transferId("3c869968-782c-4625-ba8f-b20e8c793468")
                .senderAccountNumber("36310206164628513592651084")
                .recipientAccountNumber("01112962489695456055040725")
                .amount(BigDecimal.valueOf(1500.50))
                .currencyType("PLN")
                .transferDate(LocalDateTime.parse("2024-12-19T14:30:00"))
                .transferStatus("COMPLETED")
                .build();
    }

    @Test
    @DisplayName("It should consume TransferEvent successfully")
    void itShouldConsumeTransferEventSuccessfully() throws InterruptedException {
        // Given
        AccountService accountService = mock(AccountService.class);
        when(accountService.updateAccountBalance(anyString(), any(BigDecimal.class)))
                .thenReturn(AccountDto.builder().build());
        kafkaTemplate.send(CREATE_TRANSFER_EVENT, transferEvent);

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(500, TimeUnit.MILLISECONDS);

        // Then
        verify(transferEventConsumer, times(1))
                .onMessage(any(ConsumerRecord.class));
    }

    @Test
    @DisplayName("It should failure with consume TransferEvent")
    void itShouldFailureWithConsumeTransferEvent() throws InterruptedException {
        // Given
        when(accountService.updateAccountBalance(anyString(), any(BigDecimal.class)))
                .thenThrow(new AccountNotExistsException("Account with id: %s not exists"));
        kafkaTemplate.send(CREATE_TRANSFER_EVENT, transferEvent);

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(500, TimeUnit.MILLISECONDS);

        // Then
        verify(transferEventConsumer, times(1))
                .onMessage(isA(ConsumerRecord.class));
    }

}