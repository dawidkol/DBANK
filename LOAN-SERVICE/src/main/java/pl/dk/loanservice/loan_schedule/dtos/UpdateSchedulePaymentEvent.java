package pl.dk.loanservice.loan_schedule.dtos;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UpdateSchedulePaymentEvent(String loanScheduleId, String transferId, LocalDateTime transferDate) {
}
