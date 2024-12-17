package pl.dk.accounts_service.account;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dk.accounts_service.account.dtos.AccountNumberDto;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

@Repository
interface AccountRepository extends JpaRepository<Account, BigInteger> {

    Optional<AccountNumberDto> findByAccountNumber(BigInteger accountNumber);

}
