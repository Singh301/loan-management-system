package com.sudhanshu.loanmanagement.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Component
public class EmiCalculator {

    public BigDecimal calculateEmi(
            BigDecimal principal,
            BigDecimal annualInterestRate,
            Integer tenureMonths) {

        double p = principal.doubleValue();

        double monthlyRate =
                annualInterestRate.doubleValue() / (12 * 100);

        double n = tenureMonths;

        double emi = (p * monthlyRate * Math.pow(1 + monthlyRate, n)) / (Math.pow(1 + monthlyRate, n) - 1);

        return BigDecimal.valueOf(emi)
                .round(new MathContext(15))
                .setScale(2, RoundingMode.HALF_UP);
    }

}




