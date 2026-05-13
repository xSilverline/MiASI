package com.mission.accesscontrol;

import org.mindrot.jbcrypt.BCrypt;
import java.util.concurrent.ConcurrentHashMap;

public class Authorization {
    private final IUserRepository repository;
    private final ConcurrentHashMap<String, Identity> cache = new ConcurrentHashMap<>();
    private Session activeSession = null;

    public Authorization(IUserRepository repository) {
        this.repository = repository;
        repository.findAll().forEach(i -> cache.put(i.getLogin(), i));
    }


    public synchronized String login(String login, String password) throws Exception {
        // session validation
        if (activeSession != null) {
            throw new Exception("Access denied. Another active session already exists.");
        }

        Identity identity = cache.get(login);

        if (identity == null) {
            identity = repository.findByLogin(login);
            if (identity != null) {
                cache.put(login, identity);
            }
        }

        // password check
        if (identity == null || !BCrypt.checkpw(password, identity.getPasswordHash())) {
            throw new Exception("Login failed.");
        }

        activeSession = new Session(login);
        return activeSession.getSessionToken();
    }



    public synchronized void logout(String token) throws Exception {
        if (activeSession != null && activeSession.getSessionToken().equals(token)) {
            activeSession = null;
        } else {
            throw new Exception("Invalid session token.");
        }
    }


}