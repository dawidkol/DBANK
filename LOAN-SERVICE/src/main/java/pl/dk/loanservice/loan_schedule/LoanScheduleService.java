package pl.dk.loanservice.loan_schedule;

import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleDto;
import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleEvent;
import pl.dk.loanservice.loan_schedule.dtos.UpdateSchedulePaymentEvent;

import java.util.List;

interface LoanScheduleService {

    void createSchedule(LoanScheduleEvent loanScheduleEvent);

    void updatePaymentInstallmentStatus(UpdateSchedulePaymentEvent event);

    void setPaymentStatusAsPaidLate();

    List<LoanScheduleDto> getLoanSchedule(String loan_id);

}
