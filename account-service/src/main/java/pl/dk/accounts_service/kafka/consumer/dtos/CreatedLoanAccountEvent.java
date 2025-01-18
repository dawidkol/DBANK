package pl.dk.accounts_service.kafka.consumer.dtos;

import lombok.Builder;

@Builder
public record CreatedLoanAccountEvent(String accountNumber, String loanId) {
}
