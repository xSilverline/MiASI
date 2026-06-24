package miasi.backend.authorization.infrastructure.out.persistence.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import miasi.backend.authorization.application.port.out.UserRepositoryPort;
import miasi.backend.authorization.domain.model.Identity;
import miasi.backend.common.infrastructure.out.persistence.json.JsonFileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
public class JsonUserRepository implements UserRepositoryPort {

  JsonFileStorage<Identity> database = new JsonFileStorage<>(Identity.class);

  @Value("${database.filename.users}")
  String filePath;

  @Override
  public Identity findByLogin(String login) {
    return findAll().stream()
        .filter(i -> i.getLogin().equalsIgnoreCase(login))
        .findFirst()
        .orElse(null);
  }

  @Override
  public Collection<Identity> findAll() {
    List<Identity> identities = database.loadListFromFile(filePath);
    if (identities == null) {
      return new ArrayList<>();
    }

    return identities;
  }

  @Override
  public boolean exists(String login) {
    return findByLogin(login) != null;
  }
}
