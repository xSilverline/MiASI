package miasi.backend.database;

import miasi.backend.domains.authorization.IUserRepository;
import miasi.backend.domains.authorization.Identity;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonUserRepository implements IUserRepository {

  JsonFileStorage database = new JsonFileStorage();

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

    List<Identity> identities = database.loadFromFile(filePath);

    if (identities == null)
      return new ArrayList<>();

    return identities;
  }

  @Override
  public boolean exists(String login) {
    return findByLogin(login) != null;
  }
}