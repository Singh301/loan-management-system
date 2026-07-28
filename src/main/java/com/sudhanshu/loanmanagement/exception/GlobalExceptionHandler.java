package com.sudhanshu.loanmanagement.exception;

import com.sudhanshu.loanmanagement.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Customer Already Exists Exception
     */
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleCustomerAlreadyExistsException(
            CustomerAlreadyExistsException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * User Already Exists Exception
     */
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ApiResponse> handleUserAlreadyExistsException(
            UserAlreadyExistsException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Resource Not Found Exception
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Validation Exception
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult()
                .getFieldError()
                .getDefaultMessage();

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(errorMessage)
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Generic Exception
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(LoanAlreadyProcessedException.class)
    public ResponseEntity<ApiResponse> handleLoanAlreadyProcessedException(
            LoanAlreadyProcessedException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(
            IllegalArgumentException ex) {

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

}