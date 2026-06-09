package miasi.backend.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import miasi.backend.domains.authorization.IUserRepository;
import miasi.backend.domains.authorization.Identity;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JsonUserRepository implements IUserRepository {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Identity findByLogin(String login) {
        return findAll().stream()
                .filter(i -> i.getLogin().equalsIgnoreCase(login))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Collection<Identity> findAll() {
        try {

            File file = new ClassPathResource("database/users.json").getFile();

            return objectMapper.readValue(file, new TypeReference<List<Identity>>() {});

        } catch (IOException e) {
            System.err.println("Cannot read users.json file." + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean exists(String login) {
        return findByLogin(login) != null;
    }
}