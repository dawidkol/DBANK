package pl.dk.loanservice.loan_schedule.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LoanScheduleDto(String id,
                              BigDecimal installment,
                              LocalDate paymentDate,
                              LocalDate deadline,
                              String paymentStatus) {
}
