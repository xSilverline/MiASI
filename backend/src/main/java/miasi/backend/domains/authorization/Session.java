package miasi.backend.domains.authorization;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Session {
  private final String sessionToken;
  private final String login;

  public Session(String login) {
    this.sessionToken = UUID.randomUUID().toString();
    this.login = login;
  }
}
