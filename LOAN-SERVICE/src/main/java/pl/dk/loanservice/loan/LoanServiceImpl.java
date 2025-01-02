package pl.dk.loanservice.loan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.dk.loanservice.credit.CreditScoreCalculator;
import pl.dk.loanservice.exception.*;
import pl.dk.loanservice.httpClient.AccountServiceFeignClient;
import pl.dk.loanservice.httpClient.TransferServiceFeignClient;
import pl.dk.loanservice.httpClient.UserServiceFeignClient;
import pl.dk.loanservice.httpClient.dtos.UserDto;
import pl.dk.loanservice.loan.dtos.*;
import pl.dk.loanservice.loan_details.LoanDetails;
import pl.dk.loanservice.loan_details.LoanDetailsRepository;
import pl.dk.loanservice.loan_schedule.dtos.UpdateSchedulePaymentEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static pl.dk.loanservice.kafka.KafkaConstants.CREATE_LOAN_ACCOUNT;

@Service
@RequiredArgsConstructor
@Slf4j
class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final AccountServiceFeignClient accountServiceFeignClient;
    private final UserServiceFeignClient userServiceFeignClient;
    private final CreditScoreCalculator creditScoreCalculator;
    private final KafkaTemplate<String, CreateLoanAccountDto> kafkaTemplate;
    private final TransferServiceFeignClient transferServiceFeignClient;
    private final LoanDetailsRepository loanDetailsRepository;

    @Override
    @Transactional
    public LoanDto createLoan(CreateLoanDto createLoanDto) {
        validateRequest(createLoanDto);
        Loan loanToSave = createLoanToSave(createLoanDto);
        Loan savedLoan = loanRepository.save(loanToSave);
        LoanDto result = LoanDtoMapper.map(savedLoan);
        buildAndPublishCreateLoanEvent(createLoanDto, result.id());
        return result;
    }

    private static Loan createLoanToSave(CreateLoanDto createLoanDto) {
        Loan loanToSave = LoanDtoMapper.map(createLoanDto);
        loanToSave.setStatus(LoanStatus.PENDING);
        loanToSave.setRemainingAmount(createLoanDto.amount());
        long loanPeriod = Period.between(createLoanDto.startDate(), createLoanDto.endDate()).toTotalMonths();
        int numberOfInstallments = (int) loanPeriod;
        loanToSave.setNumberOfInstallments(numberOfInstallments);
        return loanToSave;
    }

    private void buildAndPublishCreateLoanEvent(CreateLoanDto createLoanDto, String loanId) {
        LocalDate startDate = createLoanDto.startDate();
        LocalDate endDate = createLoanDto.endDate();
        long loanPeriod = Period.between(startDate, endDate).toTotalMonths();
        int numberOfInstallments = (int) loanPeriod;
        LoanEvent loanEvent = LoanEvent.builder()
                .userId(createLoanDto.userId())
                .loanId(loanId)
                .avgExpenses(createLoanDto.avgExpenses())
                .avgIncome(createLoanDto.avgIncome())
                .existingLoanRepayment(createLoanDto.existingLoanRepayments())
                .amountOfLoan(createLoanDto.amount())
                .numberOfInstallments(numberOfInstallments)
                .interestRate(createLoanDto.interestRate())
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

    @Override
    @ApplicationModuleListener
    public void evaluateCreditWorthiness(LoanEvent loanEvent) {
        ResponseEntity<BigDecimal> avgLast12Moths = accountServiceFeignClient.getAvgLast12Moths(loanEvent.userId());
        if (avgLast12Moths.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
            throw new UserNotFoundException("Accounts with userUd: %s not exists".formatted(loanEvent.userId()));
        }
        BigDecimal avgBalance = avgLast12Moths.getBody();
        BigDecimal score = creditScoreCalculator.calculateCreditScore(
                loanEvent.avgIncome(),
                loanEvent.avgExpenses(),
                avgBalance,
                loanEvent.existingLoanRepayment());
        BigDecimal monthlyInstallment = calculateMonthlyInstallment(
                loanEvent.amountOfLoan(),
                loanEvent.interestRate(),
                loanEvent.numberOfInstallments());
        if (score.compareTo(monthlyInstallment) > 0) {
            loanRepository.updateLoansStatus(LoanStatus.APPROVED, loanEvent.loanId());
            kafkaTemplate.send(CREATE_LOAN_ACCOUNT, loanEvent.loanId(), createLoanAccount(loanEvent.userId(), loanEvent.loanId()));
        } else {
            loanRepository.updateLoansStatus(LoanStatus.REJECTED, loanEvent.loanId());
        }
    }

    @Override
    public BigDecimal calculateMonthlyInstallment(BigDecimal amountOfLoan, BigDecimal interestRate, Integer months) {
        interestRate = interestRate.movePointLeft(2);
        BigDecimal q = BigDecimal.ONE.add(interestRate.divide(BigDecimal.valueOf(12), 3, RoundingMode.HALF_UP));
        BigDecimal qPow = q.pow(months);
        return (amountOfLoan.multiply(qPow).multiply(q.subtract(BigDecimal.ONE)))
                .divide(qPow.subtract(BigDecimal.ONE), 2, RoundingMode.HALF_UP);
    }

    private CreateLoanAccountDto createLoanAccount(String userId, String loanId) {
        return CreateLoanAccountDto.builder()
                .userId(userId)
                .accountType(AccountType.LOAN.name())
                .balance(BigDecimal.ZERO)
                .loanId(loanId)
                .build();
    }

    @Override
    public List<LoanDto> getAllUsersLoans(String userId, int page, int size) {
        return loanRepository.findAllByUserId(userId, PageRequest.of(page - 1, size))
                .stream()
                .map(LoanDtoMapper::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TransferDto payInstallment(CreateLoanInstallmentTransfer createLoanInstallmentTransfer) {
        String loanId = createLoanInstallmentTransfer.loanId();
        LoanDetails loanDetails = loanDetailsRepository.findByLoan_id(loanId).orElseThrow(() ->
                new LoanDetailsNotExistsException("Loan with id: %s not found".formatted(loanId)));

        Loan loan = loanRepository.findById(loanId).orElseThrow(() ->
                new LoanNotExistsException("Loan with id: %s not found".formatted(loanId)));

        BigDecimal monthlyInstallment = calculateMonthlyInstallment(loan.getAmount(), loan.getInterestRate(), loan.getNumberOfInstallments());
        CreateTransferDto createTransferDto = buildCreateTransferDtoObject(createLoanInstallmentTransfer, loanDetails, monthlyInstallment);

        ResponseEntity<TransferDto> transferDtoResponseEntity = transferServiceFeignClient.createTransfer(createTransferDto);
        HttpStatusCode responseStatusCode = transferDtoResponseEntity.getStatusCode();
        if (responseStatusCode.isSameCodeAs(HttpStatus.CREATED)) {
            applicationEventPublisher.publishEvent(new UpdateSchedulePaymentEvent(loanId));
            return transferDtoResponseEntity.getBody();
        }
        if (responseStatusCode.isSameCodeAs(HttpStatus.BAD_REQUEST)) {
            throw new ResponseStatusException(responseStatusCode);
        }
        if (responseStatusCode.is5xxServerError()) {
            throw new TransferServiceUnavailableException("Transfer Service unavailable");
        }

        throw new PayInstallmentException("Pay installment could not be processed, try again later");
    }

    private CreateTransferDto buildCreateTransferDtoObject(CreateLoanInstallmentTransfer createLoanInstallmentTransfer, LoanDetails loanDetails, BigDecimal monthlyInstallment) {
        return CreateTransferDto.builder()
                .senderAccountNumber(createLoanInstallmentTransfer.senderAccountNumber())
                .recipientAccountNumber(loanDetails.getLoanAccountNumber())
                .amount(monthlyInstallment)
                .currencyType("PLN")
                .transferDate(createLoanInstallmentTransfer.transferDate())
                .description(createLoanInstallmentTransfer.description())
                .build();
    }
}