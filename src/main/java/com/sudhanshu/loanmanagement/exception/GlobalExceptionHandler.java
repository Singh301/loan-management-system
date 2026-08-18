package com.sudhanshu.loanmanagement.exception;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;

import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Customer Already Exists
     */
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleCustomerAlreadyExistsException(
            CustomerAlreadyExistsException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    /**
     * User Already Exists
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    /**
     * Resource Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
    }

    /**
     * Validation Errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                errorMessage
        );
    }



    /**
     * Username Not Found
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password."
        );
    }

    /**
     * Invalid JSON / Request Body
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid or missing request body."
        );
    }

    /**
     * Invalid Path Variable / Request Parameter
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String parameterName = ex.getName();
        String value = ex.getValue() != null
                ? ex.getValue().toString()
                : "null";

        String message = String.format(
                "Invalid value '%s' for parameter '%s'.",
                value,
                parameterName
        );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }

    /**
     * Loan Already Processed
     */
    @ExceptionHandler(LoanAlreadyProcessedException.class)
    public ResponseEntity<ApiResponse> handleLoanAlreadyProcessedException(
            LoanAlreadyProcessedException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse> handleMissingRequestParameter(
            MissingServletRequestParameterException ex) {

        String message = String.format(
                "Required request parameter '%s' is missing.",
                ex.getParameterName()
        );

        ApiResponse response = new ApiResponse(
                false,
                message,
                null
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }


    /**
     * Illegal Argument
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
    }

    /**
     * Access Denied
     */
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex) {

        log.warn("Authorization denied: {}", ex.getMessage());

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access denied. You do not have permission to access this resource."
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse> handleAccessDeniedException(
            AccessDeniedException ex) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage()
        );
    }

    @ExceptionHandler(org.springframework.security.authentication.DisabledException.class)
    public ResponseEntity<ApiResponse> handleDisabledException(
            org.springframework.security.authentication.DisabledException ex) {

        log.warn("Disabled account login attempt.");

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Your account has been disabled. Please contact the administrator.")
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
    public ResponseEntity<ApiResponse> handleBadCredentials(
            org.springframework.security.authentication.BadCredentialsException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Invalid username or password.")
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.LockedException.class)
    public ResponseEntity<ApiResponse> handleLocked(
            org.springframework.security.authentication.LockedException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Account is locked.")
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.CredentialsExpiredException.class)
    public ResponseEntity<ApiResponse> handleExpiredCredentials(
            org.springframework.security.authentication.CredentialsExpiredException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Password has expired.")
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }

    @ExceptionHandler(org.springframework.security.authentication.AccountExpiredException.class)
    public ResponseEntity<ApiResponse> handleExpiredAccount(
            org.springframework.security.authentication.AccountExpiredException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Account has expired.")
                .data(null)
                .build();

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
    }



    /**
     * Generic domain / business rule violation
     */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse> handleDomainException(DomainException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    /**
     * Invalid loan status transition
     */
    @ExceptionHandler(InvalidLoanStateException.class)
    public ResponseEntity<ApiResponse> handleInvalidLoanStateException(
            InvalidLoanStateException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    /**
     * Optimistic locking failure (concurrent update)
     */
    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse> handleOptimisticLockException(
            org.springframework.orm.ObjectOptimisticLockingFailureException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "The resource was modified by another transaction. Please refresh and try again."
        );
    }

    /**
     * Generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {

        log.error("Unexpected application error", ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error. Please try again later."
        );
    }

    /**
     * Common API response builder
     */
    private ResponseEntity<ApiResponse> buildResponse(
            HttpStatus status,
            String message) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(message)
                .data(null)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}




