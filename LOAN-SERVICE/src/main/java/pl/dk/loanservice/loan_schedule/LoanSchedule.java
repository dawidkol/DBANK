package pl.dk.loanservice.loan_schedule;

import jakarta.persistence.*;
import lombok.*;
import pl.dk.loanservice.loan.BaseEntity;
import pl.dk.loanservice.loan.Loan;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "loan_schedules")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
class LoanSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private BigDecimal installment;
    private LocalDate paymentDate;
    private LocalDate deadline;
    @Enumerated(value = EnumType.STRING)
    private PaymentStatus paymentStatus;
    @ManyToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

}
