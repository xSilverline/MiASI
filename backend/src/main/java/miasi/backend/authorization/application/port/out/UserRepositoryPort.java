package miasi.backend.authorization.application.port.out;

import java.util.Collection;
import miasi.backend.authorization.domain.model.Identity;

public interface UserRepositoryPort {
  Identity findByLogin(String login);

  Collection<Identity> findAll();

  boolean exists(String login);
}
