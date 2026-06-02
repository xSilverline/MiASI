package miasi.backend.database;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.File;

public class JsonFileStorage<T> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void saveToFile(T data, String filePath) {
    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(filePath), data);
  }

  public T loadFromFile(String filePath) {
    if (filePath == null)
      throw new IllegalArgumentException("filePath is null");

    File file = new File(filePath);

    if (!file.exists()) {
      return null;
    }

    return objectMapper.readValue(file, new TypeReference<T>() {
    });
  }
}
