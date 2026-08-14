package com.sudhanshu.loanmanagement.loan.service.impl;

import com.sudhanshu.loanmanagement.exception.ResourceNotFoundException;
import com.sudhanshu.loanmanagement.loan.dto.LoanProductRequestDto;
import com.sudhanshu.loanmanagement.loan.dto.LoanProductResponseDto;
import com.sudhanshu.loanmanagement.loan.entity.LoanProduct;
import com.sudhanshu.loanmanagement.loan.entity.LoanType;
import com.sudhanshu.loanmanagement.loan.repository.LoanProductRepository;
import com.sudhanshu.loanmanagement.loan.service.LoanProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.sudhanshu.loanmanagement.config.CacheConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanProductServiceImpl implements LoanProductService {

    private final LoanProductRepository loanProductRepository;

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.LOAN_PRODUCTS, allEntries = true)
    public LoanProductResponseDto createProduct(LoanProductRequestDto request) {

        if (loanProductRepository.findByProductCode(request.getProductCode()).isPresent()) {
            throw new IllegalArgumentException("Product code already exists: " + request.getProductCode());
        }

        LoanProduct product = LoanProduct.builder()
                .productCode(request.getProductCode())
                .productName(request.getProductName())
                .loanType(request.getLoanType())
                .interestRate(request.getInterestRate())
                .minTenureMonths(request.getMinTenureMonths())
                .maxTenureMonths(request.getMaxTenureMonths())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .processingFeePercent(request.getProcessingFeePercent())
                .lateFeeAmount(request.getLateFeeAmount())
                .active(true)
                .build();

        return mapToDto(loanProductRepository.save(product));
    }

    @Override
    @Cacheable(value = CacheConfig.LOAN_PRODUCTS, key = "#productId")
    public LoanProductResponseDto getProductById(Long productId) {
        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Product not found with id: " + productId));
        return mapToDto(product);
    }

    @Override
    @Cacheable(value = CacheConfig.LOAN_PRODUCTS, key = "'active'")
    public List<LoanProductResponseDto> getAllActiveProducts() {
        return loanProductRepository.findByActiveTrue()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    public List<LoanProductResponseDto> getProductsByType(LoanType loanType) {
        return loanProductRepository.findByLoanTypeAndActiveTrue(loanType)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.LOAN_PRODUCTS, allEntries = true)
    public LoanProductResponseDto updateProduct(Long productId, LoanProductRequestDto request) {
        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Product not found with id: " + productId));

        product.setProductName(request.getProductName());
        product.setInterestRate(request.getInterestRate());
        product.setMinTenureMonths(request.getMinTenureMonths());
        product.setMaxTenureMonths(request.getMaxTenureMonths());
        product.setMinAmount(request.getMinAmount());
        product.setMaxAmount(request.getMaxAmount());
        product.setProcessingFeePercent(request.getProcessingFeePercent());
        product.setLateFeeAmount(request.getLateFeeAmount());

        return mapToDto(loanProductRepository.save(product));
    }

    @Override
    @Transactional
    public void deactivateProduct(Long productId) {
        LoanProduct product = loanProductRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan Product not found with id: " + productId));
        product.setActive(false);
        loanProductRepository.save(product);
    }

    private LoanProductResponseDto mapToDto(LoanProduct product) {
        return LoanProductResponseDto.builder()
                .productId(product.getProductId())
                .productCode(product.getProductCode())
                .productName(product.getProductName())
                .loanType(product.getLoanType())
                .interestRate(product.getInterestRate())
                .minTenureMonths(product.getMinTenureMonths())
                .maxTenureMonths(product.getMaxTenureMonths())
                .minAmount(product.getMinAmount())
                .maxAmount(product.getMaxAmount())
                .processingFeePercent(product.getProcessingFeePercent())
                .lateFeeAmount(product.getLateFeeAmount())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .build();
    }
}