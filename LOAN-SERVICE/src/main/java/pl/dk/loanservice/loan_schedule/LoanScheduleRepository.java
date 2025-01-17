package pl.dk.loanservice.loan_schedule;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dk.loanservice.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.Collection;
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

    List<LoanSchedule> findAllByLoan_id(String loanId);

    List<LoanSchedule> findAllByPaymentStatus(PaymentStatus paymentStatus, Pageable pageable);

    List<LoanSchedule> findAllByPaymentStatus(PaymentStatus paymentStatus);

    Optional<LoanSchedule> findByTransferId(String transferId);

    List<LoanSchedule> findAllByPaymentStatusInAndPaymentDateIsLessThanEqual(Collection<PaymentStatus> paymentStatuses, LocalDate paymentDateIsLessThan, Pageable pageable);

    @Query("Update LoanSchedule l SET l.paymentStatus = :newStatus " +
           "WHERE l.paymentDate <= CURRENT DATE and l.paymentStatus = 'SCHEDULED'")
    @Modifying
    int updateStatusFromScheduledTo(@Param("newStatus") PaymentStatus paymentStatus);

    List<LoanSchedule> findAllByDeadlineBefore(LocalDate deadlineBefore);

    List<LoanSchedule> findAllByDeadlineBeforeAndPaymentStatusIn(LocalDate deadlineBefore, Collection<PaymentStatus> paymentStatuses);
}
