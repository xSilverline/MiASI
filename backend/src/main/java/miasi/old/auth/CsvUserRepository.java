// NIE UWZGLĘDNIAMY W OFICJALNYM PROJEKCIE

package miasi.old.auth;

import miasi.backend.domains.authorization.IUserRepository;
import miasi.backend.domains.authorization.Identity;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CsvUserRepository implements IUserRepository, IUserManagementRepository {
  private final String CSV_PATH = "access_list.csv";


  public void save(Identity identity) {
    String csvLine = identity.getLogin() + "," + identity.getPasswordHash() + "\n";
    try {
      Files.write(Paths.get(CSV_PATH), csvLine.getBytes(),
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException e) {
      throw new RuntimeException("Data storage error", e);
    }
  }


  @Override
  public Identity findByLogin(String login) {
    return findAll().stream()
        .filter(i -> i.getLogin().equalsIgnoreCase(login))
        .findFirst().orElse(null);
  }

  @Override
  public Collection<Identity> findAll() {
    List<Identity> identities = new ArrayList<>();
    Path path = Paths.get(CSV_PATH);
    if (!Files.exists(path)) return identities;

    try (BufferedReader br = Files.newBufferedReader(path)) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] data = line.split(",");
        if (data.length == 2) {
          identities.add(new Identity(data[0].trim(), data[1].trim()));
        }
      }
    } catch (IOException e) {
      System.err.println("Error reading storage.");
    }
    return identities;
  }

  @Override
  public boolean exists(String login) {
    return findByLogin(login) != null;
  }
}
