package pl.dk.loanservice.loan_schedule.dtos;

import lombok.Builder;

@Builder
public record UpdateSchedulePaymentEvent(String loanId) {
}