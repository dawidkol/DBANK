package pl.dk.loanservice.loan;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.enums.LoanStatus;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {

    @Query(value = "UPDATE Loan a SET a.status = :loanStatus WHERE a.id = :loanId")
    @Modifying
    @Transactional
    void updateLoansStatus(LoanStatus loanStatus, String loanId);

    //    List<Loan> findAllByUserId(String userId);
    Page<Loan> findAllByUserId(String userId, Pageable pageable);

}
