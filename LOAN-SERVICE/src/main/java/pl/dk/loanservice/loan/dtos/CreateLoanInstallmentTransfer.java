package pl.dk.loanservice.loan.dtos;

import jakarta.validation.constraints.*;
import lombok.Builder;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record CreateLoanInstallmentTransfer(/*@NotNull
                                            @NotBlank
                                            @UUID
                                            String loanId,*/
                                            @NotNull
                                            @Pattern(regexp = "\\d{26}")
                                            String senderAccountNumber,
                                            @NotNull
                                            @FutureOrPresent
                                            LocalDateTime transferDate,
                                            @NotNull
                                            @NotBlank
                                            @Size(min = 5, max = 300)
                                            String description) {
}
