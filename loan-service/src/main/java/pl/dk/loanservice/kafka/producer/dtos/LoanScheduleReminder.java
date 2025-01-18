package pl.dk.loanservice.kafka.producer.dtos;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record LoanScheduleReminder(String loanScheduleId,
                                   BigDecimal installment,
                                   LocalDate deadline,
                                   String paymentStatus,
                                   String userId) {
}
