package pl.dk.accounts_service.account;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

@RequiredArgsConstructor
@Component
@Slf4j
class AccountNumberGeneratorImpl implements AccountNumberGenerator {

    private final AccountRepository accountRepository;
    private final Random random = new Random();

    @PostConstruct
    @Override
    public BigInteger generateAccountNumber() {
        List<Integer> list = IntStream.rangeClosed(0, 9).boxed().toList();
        boolean result;
        BigInteger createdAccountNumber;
        do {
            createdAccountNumber = generateAccount(list);
            result = checkIfExists(createdAccountNumber);
        } while (result);
        log.info("Account number created: {}", createdAccountNumber);
        return createdAccountNumber;
    }

    private BigInteger generateAccount(List<Integer> list) {
        BigInteger createdAccountNumber;
        StringBuilder accountNumberBuilder = new StringBuilder();
        for (int i = 0; i < 26; i++) {
            int random = this.random.nextInt(9);
            accountNumberBuilder.append(list.get(random));
        }
        createdAccountNumber = new BigInteger(accountNumberBuilder.toString());
        return createdAccountNumber;
    }

    private boolean checkIfExists(BigInteger createdAccountNumber) {
        AtomicBoolean result = new AtomicBoolean(false);
        accountRepository.findByAccountNumber(createdAccountNumber)
                .ifPresent(a -> {
                    result.set(true);
                });
        return result.get();
    }
}
