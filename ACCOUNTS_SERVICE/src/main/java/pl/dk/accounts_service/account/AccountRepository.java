package pl.dk.accounts_service.account;

import org.reactivestreams.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dk.accounts_service.account.dtos.AccountNumberDto;

import java.math.BigInteger;
import java.util.Optional;

@Repository
interface AccountRepository extends JpaRepository<Account, String> {

    Optional<AccountNumberDto> findByAccountNumber(String accountNumber);

}
