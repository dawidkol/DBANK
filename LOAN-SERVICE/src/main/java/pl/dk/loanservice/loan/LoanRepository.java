package pl.dk.loanservice.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface LoanRepository extends JpaRepository<Loan, String> {
}
