package pl.dk.loanservice.loan_schedule;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleDto;

import java.util.List;

@RestController
@RequestMapping("/loan-schedules")
@RequiredArgsConstructor
class LoanScheduleController {

    private final LoanScheduleService loanScheduleService;

    @GetMapping("/{loanId}")
    public ResponseEntity<List<LoanScheduleDto>> getLoanSchedule(@PathVariable String loanId) {
        List<LoanScheduleDto> loanSchedule = loanScheduleService.getLoanSchedule(loanId);
        return ResponseEntity.ok(loanSchedule);
    }
}
