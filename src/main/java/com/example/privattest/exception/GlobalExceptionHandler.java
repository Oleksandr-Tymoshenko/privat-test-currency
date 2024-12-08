package com.example.privattest.exception;

import com.example.privattest.dto.ErrorResponseDto;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(this::getErrorMessage)
                .toList();
        body.put("errors: ", errors);
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(CurrencyDataNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCurrencyDataNotFoundException(
            CurrencyDataNotFoundException ex) {
        log.warn("Currency data not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ErrorResponseDto(1, ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("User input validation error: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto(1, ex.getMessage()));
    }

    private String getErrorMessage(ObjectError e) {
        if (e instanceof FieldError error) {
            String field = error.getField();
            String message = error.getDefaultMessage();
            return field + " " + message;
        }
        return e.getDefaultMessage();
    }
}
