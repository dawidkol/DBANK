package pl.dk.accounts_service.account.dtos;

import lombok.Builder;

import java.math.BigInteger;

public record AccountNumberDto(BigInteger accountNumber) {
}
