package pl.dk.notification_service.failed_message.loan_schedule;

import jakarta.persistence.*;
import lombok.*;
import pl.dk.notification_service.failed_message.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_reminder_retry")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LoanReminderRetry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String loanScheduleId;
    private BigDecimal installment;
    private LocalDate deadline;
    private String paymentStatus;
    private String userId;
    private Boolean sent;

    @Builder
    public LoanReminderRetry(LocalDateTime createdAt,
                             String createdBy,
                             LocalDateTime updatedAt,
                             String updatedBy,
                             String id,
                             String loanScheduleId,
                             BigDecimal installment,
                             LocalDate deadline,
                             String paymentStatus,
                             String userId,
                             Boolean sent) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.loanScheduleId = loanScheduleId;
        this.installment = installment;
        this.deadline = deadline;
        this.paymentStatus = paymentStatus;
        this.userId = userId;
        this.sent = sent;
    }
}
