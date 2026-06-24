package miasi.backend.database;

import java.io.File;
import java.util.List;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

public class JsonFileStorage<T> {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Class<T> type;

  public JsonFileStorage(Class<T> type) {
    this.type = type;
  }

  public void saveListToFile(List<T> data, String filePath) {
    File file = new File(filePath);
    file.getParentFile().mkdirs();
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
  }

  public List<T> loadListFromFile(String filePath) {
    File file = new File(filePath);

    if (!file.exists()) {
      System.out.println("File " + filePath + " not exist. Returning empty data...");
      return null;
    }

    JavaType listType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, type);
    return objectMapper.readValue(file, listType);
  }
}