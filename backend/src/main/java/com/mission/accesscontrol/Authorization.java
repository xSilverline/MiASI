package com.mission.accesscontrol;

import org.mindrot.jbcrypt.BCrypt;
import java.io.*;
import java.nio.file.*;
import java.util.concurrent.ConcurrentHashMap;

public class Authorization {
    private final ConcurrentHashMap<String, Identity> repository = new ConcurrentHashMap<>();
    private Session activeSession = null;
    private final String CSV_PATH = "access_list.csv";

    public Authorization() {
        loadAccessList();
    }

    private void loadAccessList() {
        try (BufferedReader br = Files.newBufferedReader(Paths.get(CSV_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    repository.put(data[0].trim(), new Identity(data[0].trim(), data[1].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load access list file.");
        }
    }

    public synchronized String login(String login, String password) throws Exception {
        // session validation
        if (activeSession != null) {
            throw new Exception("Access denied. Another active session already exists.");
        }

        Identity identity = repository.get(login);

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

    // Helper method to check access in other modules
    public boolean isAuthenticated(String token) {
        return activeSession != null && activeSession.getSessionToken().equals(token);
    }
}