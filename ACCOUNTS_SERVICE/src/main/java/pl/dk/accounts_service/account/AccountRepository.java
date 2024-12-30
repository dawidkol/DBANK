package pl.dk.accounts_service.account;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dk.accounts_service.account.dtos.AccountNumberDto;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<AccountNumberDto> findByAccountNumber(String accountNumber);
    Page<Account> findAllByUserId(String userId, Pageable pageable);

}
