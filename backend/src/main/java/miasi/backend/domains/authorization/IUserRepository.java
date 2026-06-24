package miasi.backend.domains.authorization;

import java.util.Collection;

public interface IUserRepository {
  Identity findByLogin(String login);

  Collection<Identity> findAll();

  boolean exists(String login);
}
