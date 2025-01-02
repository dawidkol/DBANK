package pl.dk.loanservice.loan_schedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanScheduleRepository extends JpaRepository<LoanSchedule, String> {

    List<LoanSchedule> findAllByLoan_idAndPaymentStatusOrPaymentStatus(String loanId, PaymentStatus paymentStatus, PaymentStatus paymentStatus1);

    Optional<LoanSchedule> findFirstByLoan_Id(String loanId);

    @Modifying
    @Query("UPDATE LoanSchedule ls " +
           "SET ls.paymentStatus = :newStatus " +
           "WHERE ls.deadline < CURRENT_DATE AND ls.paymentStatus = 'UNPAID'")
    int setPaymentStatusFromUnpaidTo(@Param("newStatus") PaymentStatus newStatus);

}
