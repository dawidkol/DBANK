package pl.dk.loanservice.loan_schedule;

import pl.dk.loanservice.loan_schedule.dtos.LoanScheduleDto;

class LoanScheduleDtoMapper {

    public static LoanScheduleDto map(LoanSchedule loanSchedule) {
        return LoanScheduleDto.builder()
                .id(loanSchedule.getId())
                .installment(loanSchedule.getInstallment())
                .paymentDate(loanSchedule.getPaymentDate())
                .deadline(loanSchedule.getDeadline())
                .paymentStatus(loanSchedule.getPaymentStatus().name())
                .build();

    }
}
