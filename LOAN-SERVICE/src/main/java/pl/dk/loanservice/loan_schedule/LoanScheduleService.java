package pl.dk.loanservice.loan_schedule;

import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleEvent;

interface LoanScheduleService {

    void createSchedule(LoanScheduleEvent loanScheduleEvent);

}
