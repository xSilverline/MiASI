// NIE UWZGLĘDNIAMY W OFICJALNYM PROJEKCIE

package miasi.old.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.authorization.IUserRepository;
import miasi.backend.domains.authorization.Identity;
import miasi.old.auth.IUserManagementRepository;
import org.mindrot.jbcrypt.BCrypt;

@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserRegistrationService {
  IUserRepository readRepository;
  IUserManagementRepository writeRepository;

  public void register(String login, String password) throws Exception {
    if (readRepository.exists(login)) {
      throw new Exception("User already exists.");
    }

    String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
    writeRepository.save(new Identity(login, hash));
  }
}