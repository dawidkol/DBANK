package pl.dk.accounts_service.account_balance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, String> {

    Optional<AccountBalance> findFirstByAccount_AccountNumberAndCurrencyType(String accountAccountNumber, CurrencyType currencyType);
}
