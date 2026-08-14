package com.sudhanshu.loanmanagement.loan.mapper;

import com.sudhanshu.loanmanagement.loan.dto.EmiScheduleResponseDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.EmiSchedule;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

/**
 * MapStruct mapper for Loan domain.
 * Generated implementation at compile time.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LoanMapStructMapper {

    @Mapping(target = "customerId", source = "customer.customerId")
    LoanResponseDto toResponseDto(Loan loan);

    @Mapping(target = "emiNumber", source = "installmentNumber")
    @Mapping(target = "principalAmount", source = "principalComponent")
    @Mapping(target = "interestAmount", source = "interestComponent")
    @Mapping(target = "remainingBalance", source = "outstandingPrincipalAfter")
    EmiScheduleResponseDto toEmiDto(EmiSchedule schedule);
}
