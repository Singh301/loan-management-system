package com.sudhanshu.loanmanagement.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around(
            "execution(* com.sudhanshu.loanmanagement..service..*(..))"
    )
    public Object logServiceExecution(
            ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.currentTimeMillis();

        String className =
                joinPoint.getTarget()
                        .getClass()
                        .getSimpleName();

        String method =
                joinPoint.getSignature()
                        .getName();

        log.info("Entering {}.{}()", className, method);

        try {

            Object result =
                    joinPoint.proceed();

            long execution =
                    System.currentTimeMillis() - start;

            log.info(
                    "Completed {}.{}() in {} ms",
                    className,
                    method,
                    execution
            );

            return result;

        } catch (Exception ex) {

            log.error(
                    "Exception in {}.{}() : {}",
                    className,
                    method,
                    ex.getMessage()
            );

            throw ex;
        }

    }

}




