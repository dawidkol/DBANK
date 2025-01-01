package pl.dk.loanservice.loan_schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
interface LoanScheduleRepository extends JpaRepository<LoanSchedule, String> {

    List<LoanSchedule> findAllByLoan_idAndPaymentStatusOrPaymentStatus(String loanId, PaymentStatus paymentStatus, PaymentStatus paymentStatus1);
}
