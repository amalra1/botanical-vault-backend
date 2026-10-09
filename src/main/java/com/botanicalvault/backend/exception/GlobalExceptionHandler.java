package com.botanicalvault.backend.exception;

import java.util.Map;
import java.util.stream.Collectors;

import com.botanicalvault.backend.constants.ErrorCode;
import com.botanicalvault.backend.dto.response.ErrorResponseDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String RETRY_AFTER_FIELD = "retryAfterSeconds";

    @ExceptionHandler(LoginThrottledException.class)
    public ResponseEntity<ErrorResponseDTO> handleLoginThrottled(LoginThrottledException ex) {
        String retryAfter = String.valueOf(ex.retryAfterSeconds());
        ErrorResponseDTO body = new ErrorResponseDTO(
                ex.getErrorCode().name(), ex.getMessage(), Map.of(RETRY_AFTER_FIELD, retryAfter));
        return ResponseEntity.status(ex.getErrorCode().status())
                .header(HttpHeaders.RETRY_AFTER, retryAfter)
                .body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponseDTO> handleBusinessException(BusinessException ex) {
        return respond(ex.getErrorCode(), ex.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, this::messageOf, (first, second) -> first));
        return respond(ErrorCode.VALIDATION_FAILED, "Invalid request", fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return respond(ErrorCode.MALFORMED_REQUEST, "Request body is missing or malformed", Map.of());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponseDTO> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        return respond(ErrorCode.FILE_TOO_LARGE, "Uploaded files are too large", Map.of());
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ErrorResponseDTO> handleMalformedMultipart(Exception ex) {
        return respond(ErrorCode.MALFORMED_REQUEST, "Multipart request is missing or malformed", Map.of());
    }

    private ResponseEntity<ErrorResponseDTO> respond(ErrorCode errorCode, String message, Map<String, String> fields) {
        ErrorResponseDTO body = new ErrorResponseDTO(errorCode.name(), message, fields);
        return ResponseEntity.status(errorCode.status()).body(body);
    }

    private String messageOf(FieldError fieldError) {
        return fieldError.getDefaultMessage() == null ? "" : fieldError.getDefaultMessage();
    }
}
