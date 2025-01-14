package pl.dk.notification_service.loan_reminder;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanReminderRepository extends JpaRepository<LoanReminderRetry, String> {

    Page<LoanReminderRetry> findAllByDeadlineIsLessThanEqualAndSent(LocalDate deadlineBefore, Boolean sent, Pageable pageable);

    Page<LoanReminderRetry> findAllByDeadlineIsBefore(LocalDate deadlineAfter, Pageable pageable);
}
