package pl.dk.loanservice.loan;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dk.loanservice.exception.LoanNotExistsException;
import pl.dk.loanservice.loan.dtos.CreateLoanDto;
import pl.dk.loanservice.loan.dtos.LoanDto;

@Service
@RequiredArgsConstructor
class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public LoanDto createLoan(CreateLoanDto createLoanDto) {
        Loan loanToSave = LoanDtoMapper.map(createLoanDto);
        loanToSave.setStatus(LoanStatus.PENDING);
        loanToSave.setRemainingAmount(createLoanDto.amount());
        Loan savedLoan = loanRepository.save(loanToSave);
        return LoanDtoMapper.map(savedLoan);
    }

    @Override
    public LoanDto getLoanById(String loanId) {
        return loanRepository.findById(loanId)
                .map(LoanDtoMapper::map)
                .orElseThrow(() ->
                        new LoanNotExistsException("Loan with id: %s not found".formatted(loanId)));
    }
}
