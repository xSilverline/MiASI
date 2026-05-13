package com.mission.accesscontrol;
import java.io.*;
import java.nio.file.*;
import java.util.*;
public class CsvUserRepository implements IUserRepository {
    private final String CSV_PATH = "access_list.csv";

    @Override
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
