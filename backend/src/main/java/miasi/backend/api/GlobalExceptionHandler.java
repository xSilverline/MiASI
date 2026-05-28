package miasi.backend.api;

import jakarta.servlet.http.HttpServletRequest;
import miasi.backend.api.jsons.BasicResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<BasicResponseEntity> universalHandler(Exception ex, HttpServletRequest req) {
    return ResponseEntity.badRequest().body(BasicResponseEntity.error(ex.getMessage()));
  }
}
