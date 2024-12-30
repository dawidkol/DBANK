package pl.dk.loanservice.loanDetails;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanDetailsRepository extends JpaRepository<LoanDetails, String> {

    Optional<LoanDetails> findByLoan_id(String loanId);
}
