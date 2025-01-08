package pl.dk.accounts_service.account_balance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dk.accounts_service.enums.CurrencyType;

import java.util.Optional;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, String> {

    Optional<AccountBalance> findFirstByAccount_AccountNumberAndCurrencyType(String accountAccountNumber, CurrencyType currencyType);
}
