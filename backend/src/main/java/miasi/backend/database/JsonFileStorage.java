package miasi.backend.database;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

public class JsonFileStorage {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void saveToFile(Object data, String filePath) {
    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(filePath), data);
  }

  public <T> T loadFromFile(String filePath) {
    return objectMapper.readValue(new File(filePath), new TypeReference<T>() {
    });
  }
}
