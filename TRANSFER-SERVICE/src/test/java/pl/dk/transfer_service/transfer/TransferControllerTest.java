package pl.dk.transfer_service.transfer;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.transfer_service.httpClient.AccountFeignClient;
import pl.dk.transfer_service.httpClient.dtos.AccountDto;
import pl.dk.transfer_service.kafka.KafkaConstants;
import pl.dk.transfer_service.transfer.dtos.CreateTransferDto;
import pl.dk.transfer_service.transfer.dtos.TransferDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static pl.dk.transfer_service.kafka.KafkaConstants.ACCOUNT_DTO_TRUSTED_PACKAGE;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = KafkaConstants.CREATE_TRANSFER_EVENT)
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
@Transactional
class TransferControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockitoBean
    private AccountFeignClient accountFeignClient;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    private Consumer<String, AccountDto> consumer;

    String senderAccount;
    String recipientAccount;

    AccountDto senderDto;
    AccountDto recipientDto;
    CreateTransferDto createTransferDto;

    @BeforeEach
    void setUp() {
        Map<String, Object> configs = KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        JsonDeserializer<AccountDto> userDtoJsonDeserializer = new JsonDeserializer<>();
        userDtoJsonDeserializer.addTrustedPackages(ACCOUNT_DTO_TRUSTED_PACKAGE);
        consumer = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), userDtoJsonDeserializer).createConsumer();
        embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);

        senderAccount = "36310206164628513592651084";
        recipientAccount = "01112962489695456055040725";

        String accountType = "CREDIT";
        senderDto = AccountDto.builder()
                .accountNumber(senderAccount)
                .accountType(accountType)
                .balance(BigDecimal.valueOf(100000))
                .userId("979e0762-0536-480b-890d-843715dd3be6")
                .active(true)
                .build();

        recipientDto = AccountDto.builder()
                .accountNumber(recipientAccount)
                .accountType(accountType)
                .balance(BigDecimal.ZERO)
                .userId("eb285c17-54a2-45df-9271-a0e161e9db1d")
                .active(true)
                .build();

        createTransferDto = CreateTransferDto.builder()
                .senderAccountNumber(senderAccount)
                .recipientAccountNumber(recipientAccount)
                .amount(BigDecimal.valueOf(1500.50))
                .currencyType("PLN")
                .transferDate(LocalDateTime.now().plusMonths(1))
                .description("Payment for invoice #12345")
                .build();
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    @DisplayName("It should return 404 for invalid recipient account and 201 for valid transfer with Kafka event")
    void itShouldReturn404ForInvalidRecipientAccountAnd201ForValidTransferWithKafkaEvent() {
        // 1. User wants to create transfer with invalid recipient account. Expected status code: 404 NOT_FOUND
        // Given
        Mockito.when(accountFeignClient.getAccountById(senderAccount))
                .thenReturn(ResponseEntity.of(Optional.of(senderDto)));
        Mockito.when(accountFeignClient.getAccountById(recipientAccount))
                .thenReturn(ResponseEntity.of(Optional.empty()))
                .thenReturn(ResponseEntity.of(Optional.of(recipientDto)));

        // When
        ResponseEntity<Object> transferDtoResponseEntity404 = testRestTemplate.exchange(
                "/transfers",
                HttpMethod.POST,
                new HttpEntity<>(createTransferDto),
                new ParameterizedTypeReference<>() {
                });

        // Then
        assertAll(() -> {
                    Mockito.verify(accountFeignClient, Mockito.times(2)).getAccountById(Mockito.any(String.class));
                    assertTrue(transferDtoResponseEntity404.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND));
                }
        );

        // 2. User wants to create transfer with valid data. Expected status code: 201 CREATED
        // When
        ResponseEntity<TransferDto> transferDtoResponseEntity = testRestTemplate.postForEntity(
                "/transfers",
                createTransferDto,
                TransferDto.class);

        // Then
        ConsumerRecords<String, AccountDto> records = KafkaTestUtils.getRecords(consumer);
        assertAll(() -> {
                    Mockito.verify(accountFeignClient, Mockito.times(4)).getAccountById(Mockito.any(String.class));
                    assertTrue(transferDtoResponseEntity.getStatusCode().isSameCodeAs(HttpStatus.CREATED));
                    assertEquals(1, records.count());
                }
        );

        // 3. User wants to get information about transfer: Expected status code: 200 OK
        String transferId = transferDtoResponseEntity.getBody().transferId();
        ResponseEntity<TransferDto> transferDtoResponseEntity200 = testRestTemplate.getForEntity(
                "/transfers/{transferId}",
                TransferDto.class,
                transferId);

        TransferDto transferDto200 = transferDtoResponseEntity200.getBody();
        assertAll(() -> {
            assertTrue(transferDtoResponseEntity200.getStatusCode().isSameCodeAs(HttpStatus.OK));
        });

        // 5. User wants to retrieve all his transfers. Expected status code: 200 OK
        // Given When
        ResponseEntity<List<TransferDto>> allAccountTransfers = testRestTemplate.exchange(
                "/transfers/accounts/{accountNumber}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TransferDto>>() {
                }, createTransferDto.senderAccountNumber());

        // Then
        assertAll(() -> {
            assertTrue(allAccountTransfers.getStatusCode().isSameCodeAs(HttpStatus.OK));
            Assertions.assertEquals(1, allAccountTransfers.getBody().size() );

        });

        // 6. User wants to get all transfer from given account to other account. Expected status code: 200 OK
        // Given When
        ResponseEntity<List<TransferDto>> allTransfersFromAccountToAccount = testRestTemplate.exchange(
                "/transfers?senderAccountNumber={senderAccountNumber}&recipientAccountNumber={recipientAccountNumber}",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TransferDto>>() {},
                createTransferDto.senderAccountNumber(),
                createTransferDto.recipientAccountNumber());

        // Then
        assertAll(() -> {
            assertTrue(allTransfersFromAccountToAccount.getStatusCode().isSameCodeAs(HttpStatus.OK));
            Assertions.assertEquals(1, allTransfersFromAccountToAccount.getBody().size() );
        });
    }


}