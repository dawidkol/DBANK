package pl.dk.loanservice.kafka.consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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
import pl.dk.loanservice.kafka.consumer.dtos.CreatedLoanAccountEvent;
import pl.dk.loanservice.loan.Loan;
import pl.dk.loanservice.loan.LoanRepository;
import pl.dk.loanservice.loan.LoanStatus;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static pl.dk.loanservice.kafka.KafkaConstants.LOAN_ACCOUNT_CREATED;

@SpringBootTest
@EmbeddedKafka(topics = LOAN_ACCOUNT_CREATED )
@DirtiesContext
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class AccountServiceConsumerTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaTemplate<String, CreateLoanDto> kafkaTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry endpointRegistry;

    @MockitoSpyBean
    private AccountServiceConsumer accountServiceConsumer;

    @MockitoBean
    private LoanRepository loanRepository;


    @BeforeEach
    void setUp() {
        for (MessageListenerContainer messageListenerContainer : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(messageListenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    @DisplayName("Test consume created loan account event")
    void testConsumeCreatedLoanAccountEvent() throws InterruptedException {
        // Given
        String userId = "550e8400-e29b-41d4-a716-446655440000";
        BigDecimal amount = BigDecimal.valueOf(5000);
        LocalDate startDate = LocalDate.now().plusMonths(1);
        LocalDate endDate = startDate.plusMonths(12);
        String description = "Loan for purchasing new office equipment";
        BigDecimal avgIncome = BigDecimal.valueOf(10000);
        BigDecimal avgExpenses = BigDecimal.valueOf(8500);
        BigDecimal existingLoanRepayments = BigDecimal.ZERO;

        BigDecimal interestRate = BigDecimal.valueOf(4.5);

        String id = "123e4567-e89b-12d3-a456-426614174000";
        LoanStatus status = LoanStatus.PENDING;
        BigDecimal remainingAmount = amount;

        CreateLoanDto createLoanDto = CreateLoanDto.builder()
                .userId(userId)
                .amount(amount)
                .interestRate(interestRate)
                .startDate(startDate)
                .endDate(endDate)
                .description(description)
                .avgIncome(avgIncome)
                .avgExpenses(avgExpenses)
                .existingLoanRepayments(existingLoanRepayments)
                .build();

        Loan loan = Loan.builder()
                .id(id)
                .userId(userId)
                .amount(amount)
                .interestRate(interestRate)
                .startDate(startDate)
                .endDate(endDate)
                .remainingAmount(remainingAmount)
                .status(status)
                .description(description)
                .build();

        kafkaTemplate.send(LOAN_ACCOUNT_CREATED, createLoanDto);

        // When
        Mockito.when(loanRepository.findById(any())).thenReturn(Optional.of(loan));
        CountDownLatch latch = new CountDownLatch(1);
        latch.await(500, TimeUnit.MILLISECONDS);

        // Then
        assertAll(() -> {
            verify(accountServiceConsumer, times(1))
                    .consumeCreatedEventAccount(isA(CreatedLoanAccountEvent.class));
        });

    }
}