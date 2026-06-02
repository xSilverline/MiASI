package miasi.backend.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class JsonFileStorage<T> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void saveToFile(T data, String filePath) throws IOException {
    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(filePath), data);
  }

  public T loadFromFile(String filePath) throws IOException {
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
