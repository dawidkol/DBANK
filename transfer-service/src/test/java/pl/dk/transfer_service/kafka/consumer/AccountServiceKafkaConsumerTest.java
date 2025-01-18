package pl.dk.transfer_service.kafka.consumer;

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
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import pl.dk.transfer_service.kafka.consumer.dtos.ResponseTransferEvent;
import pl.dk.transfer_service.enums.TransferStatus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.dk.transfer_service.kafka.KafkaConstants.PROCESS_TRANSFER_EVENT;

@SpringBootTest
@EmbeddedKafka(topics = PROCESS_TRANSFER_EVENT)
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class AccountServiceKafkaConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, ResponseTransferEvent> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @MockitoSpyBean
    private AccountServiceKafkaConsumer accountServiceKafkaConsumerSpy;

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    @DisplayName("It should consume ResponseTransferEvent successfully")
    void itShouldConsumeResponseTransferEventSuccessfully() throws ExecutionException, InterruptedException {
        // Given
        ResponseTransferEvent responseTransferEvent = ResponseTransferEvent.builder()
                .transferId("3c869968-782c-4625-ba8f-b20e8c793468")
                .transferStatus(TransferStatus.COMPLETED.name())
                .build();

        kafkaTemplate.send(PROCESS_TRANSFER_EVENT, responseTransferEvent).get();

        // When
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(500, TimeUnit.MILLISECONDS);

        // Then
        verify(accountServiceKafkaConsumerSpy, times(1)).onMessage(isA(ConsumerRecord.class));
    }

}