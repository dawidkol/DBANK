package pl.dk.loanservice.loanDetails;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import pl.dk.loanservice.loan.BaseEntity;
import pl.dk.loanservice.loan.Loan;

@Entity
@Table(name = "loan_details")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoanDetails extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @NotBlank
    private String loanAccountNumber;
    @OneToOne
    @JoinColumn(name = "loan_id")
    private Loan loan;

}
