package com.sudhanshu.loanmanagement.loan.service;

import com.sudhanshu.loanmanagement.loan.dto.CollateralRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.CollateralResponseDto;
import java.util.List;

public interface CollateralService {
    CollateralResponseDto addCollateral(Long loanId, CollateralRequestDto request);
    List<CollateralResponseDto> getCollateralsByLoan(Long loanId);
    void deleteCollateral(Long collateralId);
}