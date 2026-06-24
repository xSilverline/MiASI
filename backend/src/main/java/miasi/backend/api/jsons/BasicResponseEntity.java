package miasi.backend.api.jsons;

public record BasicResponseEntity(String status, String message) {
  public static BasicResponseEntity success(String message) {
    return new BasicResponseEntity("success", message);
  }

  public static BasicResponseEntity error(String message) {
    return new BasicResponseEntity("error", message);
  }
}
