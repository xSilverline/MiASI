package com.mission.accesscontrol;

import org.mindrot.jbcrypt.BCrypt;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class UserRegistration {
    private static final String CSV_PATH = "access_list.csv";

    public static void createNewCommander(String login, String plainPassword) {
        // check for existing login
        if (isLoginTaken(login)) {
            System.out.println("Registration failed: Login '" + login + "' is already taken.");
            return;
        }

        // hash the password
        String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
        String csvLine = login + "," + hashedPassword + "\n";

        // write into CSV file
        try {
            Files.write(
                    Paths.get(CSV_PATH),
                    csvLine.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            System.out.println("Successfully provisioned commander: " + login);
        } catch (IOException e) {
            System.err.println("Failed to write to access list: " + e.getMessage());
        }
    }


    private static boolean isLoginTaken(String login) {
        Path path = Paths.get(CSV_PATH);
        if (!Files.exists(path)) {
            return false;
        }

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length > 0 && data[0].trim().equalsIgnoreCase(login.trim())) {
                    return true; // the same login found
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading access list: " + e.getMessage());
        }
        return false;
    }
}