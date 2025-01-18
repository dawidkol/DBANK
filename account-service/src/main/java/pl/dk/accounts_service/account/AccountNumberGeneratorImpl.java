package pl.dk.accounts_service.account;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@Component
class AccountNumberGeneratorImpl implements AccountNumberGenerator {

    private static final Logger log = LoggerFactory.getLogger(AccountNumberGeneratorImpl.class);

    private final AccountRepository accountRepository;
    private final Random random;
    private final List<Integer> list;

    public AccountNumberGeneratorImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
        this.list = IntStream.rangeClosed(0, 9).boxed().toList();
        this.random = new Random();
    }

    @Override
    public String generateAccountNumber() {
        boolean result;
        String createdAccountNumber;
        do {
            createdAccountNumber = generateAccount(list);
            result = checkIfExists(createdAccountNumber);
        } while (result);
        log.info("Account number created: {}, no conflict", createdAccountNumber);
        return createdAccountNumber;
    }

    private String generateAccount(List<Integer> list) {
        StringBuilder accountNumberBuilder = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            this.random.nextInt(list.size());
            int digit = this.random.nextInt(list.size());
            accountNumberBuilder.append(list.get(digit));
        }
        return accountNumberBuilder.toString();
    }

    private boolean checkIfExists(String createdAccountNumber) {
        AtomicBoolean result = new AtomicBoolean(false);
        accountRepository.findByAccountNumber(createdAccountNumber)
                .ifPresent(a -> {
                    log.info("Generated account number: {} already exists, reiterate process needed", createdAccountNumber);
                    result.set(true);
                });
        return result.get();
    }
}
