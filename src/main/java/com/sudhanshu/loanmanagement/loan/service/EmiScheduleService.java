package com.sudhanshu.loanmanagement.loan.service;

import com.sudhanshu.loanmanagement.loan.dto.EmiScheduleResponseDto;

import java.util.List;

/**
 * EMI schedule generation, retrieval and overdue marking.
 */
public interface EmiScheduleService {

    /**
     * Generates (and persists) EMI schedule for an APPROVED / DISBURSED loan.
     */
    List<EmiScheduleResponseDto> generateAndPersistEmiSchedule(Long loanId);

    /**
     * Returns the persisted EMI schedule for a loan.
     */
    List<EmiScheduleResponseDto> getEmiSchedule(Long loanId);

    /**
     * Calculates EMI schedule without persisting (preview).
     */
    List<EmiScheduleResponseDto> previewEmiSchedule(Long loanId);

    /**
     * Marks overdue EMIs and updates loan status to OVERDUE / NPA.
     * Intended to be called by a scheduler.
     */
    void markOverdueLoans();
}
