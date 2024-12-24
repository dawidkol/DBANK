package pl.dk.accounts_service.account_transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dk.accounts_service.account_transaction.dtos.AccountTransactionCalculationDto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
interface AccountTransactionRepository extends JpaRepository<AccountTransaction, String> {

    List<AccountTransactionCalculationDto> findAllByAccount_UserIdAndTransactionDateIsBetween(String userId, LocalDateTime transactionDateAfter, LocalDateTime transactionDateBefore);

}
