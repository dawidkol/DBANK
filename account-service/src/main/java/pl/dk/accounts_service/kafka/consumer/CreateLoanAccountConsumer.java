package pl.dk.accounts_service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.AccountService;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.account.dtos.CreateAccountDto;
import pl.dk.accounts_service.kafka.consumer.dtos.CreateLoanAccountDto;
import pl.dk.accounts_service.kafka.consumer.dtos.CreatedLoanAccountEvent;

import java.math.BigDecimal;

import static pl.dk.accounts_service.kafka.KafkaConstants.CREATE_LOAN_ACCOUNT;
import static pl.dk.accounts_service.kafka.KafkaConstants.LOAN_ACCOUNT_CREATED;

@Component
@RequiredArgsConstructor
@Slf4j
class CreateLoanAccountConsumer {

    private final AccountService accountService;
    private final KafkaTemplate<String, CreatedLoanAccountEvent> accountDtoKafkaTemplate;

    @KafkaListener(topics = CREATE_LOAN_ACCOUNT,
            properties = {"spring.json.value.default.type=pl.dk.accounts_service.kafka.consumer.dtos.CreateLoanAccountDto"})
    @Transactional
    public void createLoanAccount(CreateLoanAccountDto createLoanAccountDto) {
        CreateAccountDto createAccountDto = CreateAccountDto.builder()
                .userId(createLoanAccountDto.userId())
                .accountType(createLoanAccountDto.accountType())
                .build();
        AccountDto account = accountService.createAccount(createAccountDto);

        CreatedLoanAccountEvent createdLoanAccountEvent = CreatedLoanAccountEvent.builder()
                .accountNumber(account.accountNumber())
                .loanId(createLoanAccountDto.loanId())
                .build();
        accountDtoKafkaTemplate.send(LOAN_ACCOUNT_CREATED, account.accountNumber(), createdLoanAccountEvent);
    }
}
