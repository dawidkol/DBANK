package pl.dk.accounts_service.account_transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AccountTransactionRepository extends JpaRepository<AccountTransaction, String> {
}
