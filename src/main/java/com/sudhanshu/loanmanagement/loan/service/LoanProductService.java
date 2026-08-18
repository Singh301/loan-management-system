package com.sudhanshu.loanmanagement.loan.service;

import com.sudhanshu.loanmanagement.loan.dto.LoanProductRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanProductResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;

import java.util.List;

public interface LoanProductService {

    LoanProductResponseDto createProduct(LoanProductRequestDto request);

    LoanProductResponseDto getProductById(Long productId);

    List<LoanProductResponseDto> getAllActiveProducts();

    List<LoanProductResponseDto> getProductsByType(LoanType loanType);

    LoanProductResponseDto updateProduct(Long productId, LoanProductRequestDto request);

    void deactivateProduct(Long productId);
}