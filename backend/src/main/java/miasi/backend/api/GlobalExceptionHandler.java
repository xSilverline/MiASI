package miasi.backend.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.management.InstanceNotFoundException;
import miasi.backend.adapter.in.web.dto.ErrorResponse;
import miasi.backend.authorization.application.exception.AuthorizationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(InstanceNotFoundException.class)
  public ResponseEntity<ErrorResponse> notFound(
      InstanceNotFoundException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), Map.of(), request);
  }

  @ExceptionHandler(NoSuchElementException.class)
  public ResponseEntity<ErrorResponse> notFound(
      NoSuchElementException ex, HttpServletRequest request) {
    return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), Map.of(), request);
  }

  @ExceptionHandler(AuthorizationException.class)
  public ResponseEntity<ErrorResponse> unauthorized(
      AuthorizationException ex, HttpServletRequest request) {
    return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", ex.getMessage(), Map.of(), request);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> validationError(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> details = new LinkedHashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      details.put(fieldError.getField(), fieldError.getDefaultMessage());
    }

    return error(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> constraintViolation(
      ConstraintViolationException ex, HttpServletRequest request) {
    Map<String, String> details = new LinkedHashMap<>();
    ex.getConstraintViolations()
        .forEach(
            violation ->
                details.put(violation.getPropertyPath().toString(), violation.getMessage()));

    return error(
        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", details, request);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> unreadableMessage(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    return error(
        HttpStatus.BAD_REQUEST,
        "MALFORMED_JSON",
        "Request body cannot be parsed",
        Map.of(),
        request);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> typeMismatch(
      MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
    return error(
        HttpStatus.BAD_REQUEST,
        "INVALID_PARAMETER",
        "Request parameter has invalid value",
        Map.of(ex.getName(), String.valueOf(ex.getValue())),
        request);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> illegalArgument(
      IllegalArgumentException ex, HttpServletRequest request) {
    return error(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), Map.of(), request);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> universalHandler(Exception ex, HttpServletRequest request) {
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_ERROR",
        "Unexpected server error",
        Map.of(),
        request);
  }

  private ResponseEntity<ErrorResponse> error(
      HttpStatus status,
      String code,
      String message,
      Map<String, String> details,
      HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(ErrorResponse.of(code, message, details, request.getRequestURI()));
  }
}
