package pl.dk.loanservice.loan;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.credit.CreditScoreCalculator;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.exception.UserNotFoundException;
import pl.dk.loanservice.httpClient.AccountServiceFeignClient;
import pl.dk.loanservice.httpClient.UserServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.AccountDto;
import pl.dk.loanservice.httpClient.dtos.UserDto;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;
import pl.dk.loanservice.loan.dtos.LoanEvent;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AccountServiceFeignClient accountServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;
    private final CreditScoreCalculator creditScoreCalculator;

    @Override
    @Transactional
    public LoanDto createLoan(CreateLoanDto createLoanDto) {
        validateRequest(createLoanDto);
        Loan loanToSave = LoanDtoMapper.map(createLoanDto);
        loanToSave.setStatus(LoanStatus.PENDING);
        loanToSave.setRemainingAmount(createLoanDto.amount());
        Loan savedLoan = loanRepository.save(loanToSave);
        LoanDto result = LoanDtoMapper.map(savedLoan);
        buildAndPublishCreateLoanEvent(createLoanDto, result.id());
        return result;
    }

    private void buildAndPublishCreateLoanEvent(CreateLoanDto createLoanDto, String loanId) {
        LoanEvent loanEvent = LoanEvent.builder()
                .userId(createLoanDto.userId())
                .loanId(loanId)
                .avgExpenses(createLoanDto.avgExpenses())
                .avgIncome(createLoanDto.avgIncome())
                .existingLoanRepayment(createLoanDto.existingLoanRepayments())
                .amountOfLoan(createLoanDto.amount())
                .build();
        applicationEventPublisher.publishEvent(loanEvent);
    }

    private void validateRequest(CreateLoanDto createLoanDto) {
        String userId = createLoanDto.userId();
        ResponseEntity<UserDto> userById = userServiceFeignClient.getUserById(userId);
        if (userById.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new UserNotFoundException("User with id: %s not exists".formatted(userId));
        }
    }

    @Override
    public LoanDto getLoanById(String loanId) {
        return loanRepository.findById(loanId)
                .map(LoanDtoMapper::map)
                .orElseThrow(() ->
                        new LoanNotExistsException("Loan with id: %s not found".formatted(loanId)));
    }

    @ApplicationModuleListener
    public void checkCreditWorthiness(LoanEvent loanEvent) {
        ResponseEntity<BigDecimal> avgLast12Moths = accountServiceFeignClient.getAvgLast12Moths(loanEvent.userId());
        if (avgLast12Moths.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new UserNotFoundException("Accounts with userUd: %s not exists".formatted(loanEvent.userId()));
        }
        BigDecimal avgBalance = avgLast12Moths.getBody();
        BigDecimal score = creditScoreCalculator.calculateCreditScore(loanEvent.avgIncome(), loanEvent.avgExpenses(), avgBalance, loanEvent.existingLoanRepayment());
        if (score.compareTo(loanEvent.amountOfLoan()) > 0) {
           loanRepository.updateLoansStatus(LoanStatus.APPROVED, loanEvent.loanId());
        } else {
            loanRepository.updateLoansStatus(LoanStatus.REJECTED, loanEvent.loanId());
        }
    }
}
