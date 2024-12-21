package pl.dk.accounts_service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account.AccountService;
import pl.dk.accounts_service.account.dtos.AccountDto;
import pl.dk.accounts_service.exception.AccountNotExistsException;
import pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent;
import pl.dk.accounts_service.kafka.producer.TransferProducerService;

import java.math.BigDecimal;

import static pl.dk.accounts_service.kafka.KafkaConstants.CREATE_TRANSFER_EVENT;

@Component
@RequiredArgsConstructor
@Slf4j
class TransferEventConsumer {

    private final TransferProducerService transferProducerService;
    private final AccountService accountService;

    @KafkaListener(topics = {CREATE_TRANSFER_EVENT},
            properties = "spring.json.value.default.type=pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent")
    @Transactional
    public void onMessage(ConsumerRecord<String, TransferEvent> consumerRecord) {
        TransferEvent transferEvent = consumerRecord.value();
        log.info("ConsumerRecord: {}", consumerRecord);
        BigDecimal amount = transferEvent.amount();
        try {
            updateAccountBalance(transferEvent.senderAccountNumber(), amount.negate());
            updateAccountBalance(transferEvent.recipientAccountNumber(), amount);
            transferProducerService.processTransferSuccessfully(transferEvent);
        } catch (AccountNotExistsException ex) {
            transferProducerService.processTransferFailure(transferEvent);
        }
    }

    private void updateAccountBalance(String accountNumber, BigDecimal amount) {
        log.info("Updating sender account balance, account number {}", accountNumber);
        AccountDto accountDto = accountService.updateAccountBalance(accountNumber, amount);
        log.info("Updated account balance, account number {}, current account balance {}", accountNumber, accountDto);
    }

}
