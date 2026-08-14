package com.sudhanshu.loanmanagement.loan.service;

import com.sudhanshu.loanmanagement.loan.dto.LoanResponseDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanStatusUpdateDto;
import com.sudhanshu.loanmanagement.loan.entity.LoanApproval;

import java.util.List;

/**
 * Handles multi-level loan approval / rejection workflow and status transitions.
 */
public interface LoanApprovalService {

    /**
     * Approve or reject a loan. Supports multi-level approval (Manager → Admin).
     *
     * @param loanId      loan to process
     * @param requestDto  target status + remarks
     * @param approverUserId current authenticated user id
     * @param approverRole   ROLE_MANAGER or ROLE_ADMIN
     */
    LoanResponseDto updateLoanStatus(Long loanId,
                                     LoanStatusUpdateDto requestDto,
                                     Long approverUserId,
                                     String approverRole);

    List<LoanApproval> getApprovalHistory(Long loanId);
}
