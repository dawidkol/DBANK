package pl.dk.accounts_service.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.accounts_service.account_balance.AccountBalanceService;
import pl.dk.accounts_service.account_balance.dtos.AccountBalanceDto;
import pl.dk.accounts_service.account_balance.dtos.UpdateAccountBalanceDto;
import pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent;
import pl.dk.accounts_service.kafka.producer.TransferProducerService;

import java.math.BigDecimal;

import static pl.dk.accounts_service.kafka.KafkaConstants.CREATE_TRANSFER_EVENT;
import static pl.dk.accounts_service.enums.TransferStatus.*;

@Component
@RequiredArgsConstructor
@Slf4j
class TransferEventConsumer {

    private final TransferProducerService transferProducerService;
    private final AccountBalanceService accountBalanceService;

    @KafkaListener(topics = {CREATE_TRANSFER_EVENT},
            properties = "spring.json.value.default.type=pl.dk.accounts_service.kafka.consumer.dtos.TransferEvent")
    @Transactional
    public void onMessage(ConsumerRecord<String, TransferEvent> consumerRecord) {
        TransferEvent transferEvent = consumerRecord.value();
        log.info("ConsumerRecord: {}", consumerRecord);
        BigDecimal amount = transferEvent.amount();
        String currencyType = transferEvent.currencyType();
        try {
            updateAccountBalance(transferEvent.senderAccountNumber(), amount.negate(), currencyType);
            updateAccountBalance(transferEvent.recipientAccountNumber(), amount, currencyType);
            transferProducerService.processTransfer(transferEvent, COMPLETED);
        } catch (RuntimeException ex) {
            transferProducerService.processTransfer(transferEvent, FAILED);
        }
    }

    private void updateAccountBalance(String accountNumber, BigDecimal updateByValue, String currencyType) {
        log.info("Updating sender account balance, account number {}", accountNumber);
        UpdateAccountBalanceDto updateAccountBalanceDto = UpdateAccountBalanceDto.builder()
                .updateByValue(updateByValue)
                .currencyType(currencyType)
                .build();
        AccountBalanceDto accountBalanceDto = accountBalanceService.updateAccountBalance(accountNumber, updateAccountBalanceDto);
        log.info("Updated account balance, account number {}, current account balance {}, currency type {}", accountNumber, accountBalanceDto.balance(), accountBalanceDto.currencyType());
    }

}
