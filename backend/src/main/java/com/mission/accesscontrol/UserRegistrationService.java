package com.mission.accesscontrol;

import org.mindrot.jbcrypt.BCrypt;

public class UserRegistrationService {
    private final IUserRepository repository;

    public UserRegistrationService(IUserRepository repository) {
        this.repository = repository;
    }

    public void register(String login, String password) throws Exception {
        if (repository.exists(login)) {
            throw new Exception("User already exists.");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(12));
        repository.save(new Identity(login, hash));
    }
}