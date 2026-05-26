package com.mission.accesscontrol;
import org.mindrot.jbcrypt.BCrypt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

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