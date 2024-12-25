package pl.dk.loanservice.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
interface LoanRepository extends JpaRepository<Loan, String> {

    @Query(value = "UPDATE Loan a SET a.status = :loanStatus WHERE a.id = :loanId")
    @Modifying
    @Transactional
    void updateLoansStatus(LoanStatus loanStatus, String loanId);

}
