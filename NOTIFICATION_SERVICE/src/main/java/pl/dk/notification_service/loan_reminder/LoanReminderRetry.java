package pl.dk.notification_service.loan_reminder;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

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
    private String id;
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
                             BigDecimal installment,
                             LocalDate deadline,
                             String paymentStatus,
                             String userId,
                             Boolean sent) {
        super(createdAt, createdBy, updatedAt, updatedBy);
        this.id = id;
        this.installment = installment;
        this.deadline = deadline;
        this.paymentStatus = paymentStatus;
        this.userId = userId;
        this.sent = sent;
    }
}
