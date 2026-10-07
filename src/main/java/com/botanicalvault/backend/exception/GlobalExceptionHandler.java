package com.botanicalvault.backend.exception;

import java.util.Map;
import java.util.stream.Collectors;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.response.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponseDTO body = new ErrorResponseDTO(errorCode.name(), ex.getMessage(), Map.of());
        return ResponseEntity.status(errorCode.status()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, this::messageOf, (first, second) -> first));
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        ErrorResponseDTO body = new ErrorResponseDTO(errorCode.name(), "Invalid request", fields);
        return ResponseEntity.status(errorCode.status()).body(body);
    }

    private String messageOf(FieldError fieldError) {
        return fieldError.getDefaultMessage() == null ? "" : fieldError.getDefaultMessage();
    }
}
