package miasi.backend.common.infrastructure.in.web.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
    String code, String message, Map<String, String> details, String path, Instant timestamp) {

  public static ErrorResponse of(
      String code, String message, Map<String, String> details, String path) {
    return new ErrorResponse(code, message, details, path, Instant.now());
  }
}
