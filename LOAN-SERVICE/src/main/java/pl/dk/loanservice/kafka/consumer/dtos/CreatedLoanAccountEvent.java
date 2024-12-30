package pl.dk.loanservice.kafka.consumer.dtos;

import lombok.Builder;

@Builder
public record CreatedLoanAccountEvent(String accountNumber, String loanId) {
}
