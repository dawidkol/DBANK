package pl.dk.loanservice.loan_schedule;

import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleEvent;
import pl.dk.loanservice.loan_schedule.dtos.UpdateSchedulePaymentEvent;

interface LoanScheduleService {

    void createSchedule(LoanScheduleEvent loanScheduleEvent);

    void updatePaymentInstallmentStatus(UpdateSchedulePaymentEvent event);

}
