package com.sudhanshu.loanmanagement.loan.service.impl;

import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.loan.dto.CollateralRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.CollateralResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.Collateral;
import com.sudhanshu.loanmanagement.loan.entity.Loan;
import com.sudhanshu.loanmanagement.loan.repository.CollateralRepository;
import com.sudhanshu.loanmanagement.loan.repository.LoanRepository;
import com.sudhanshu.loanmanagement.loan.service.CollateralService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CollateralServiceImpl implements CollateralService {

    private final CollateralRepository collateralRepository;
    private final LoanRepository loanRepository;

    @Override
    @Transactional
    public CollateralResponseDto addCollateral(Long loanId, CollateralRequestDto request) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        Collateral collateral = Collateral.builder()
                .loan(loan)
                .collateralType(request.getCollateralType())
                .description(request.getDescription())
                .estimatedValue(request.getEstimatedValue())
                .ownershipProof(request.getOwnershipProof())
                .build();

        return mapToDto(collateralRepository.save(collateral));
    }

    @Override
    public List<CollateralResponseDto> getCollateralsByLoan(Long loanId) {
        return collateralRepository.findByLoanLoanId(loanId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCollateral(Long collateralId) {
        if (!collateralRepository.existsById(collateralId)) {
            throw new ResourceNotFoundException("Collateral not found");
        }
        collateralRepository.deleteById(collateralId);
    }

    private CollateralResponseDto mapToDto(Collateral c) {
        return CollateralResponseDto.builder()
                .collateralId(c.getCollateralId())
                .loanId(c.getLoan().getLoanId())
                .collateralType(c.getCollateralType())
                .description(c.getDescription())
                .estimatedValue(c.getEstimatedValue())
                .ownershipProof(c.getOwnershipProof())
                .createdAt(c.getCreatedAt())
                .build();
    }
}