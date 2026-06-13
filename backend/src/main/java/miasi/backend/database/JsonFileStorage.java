package miasi.backend.database;

import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.List;

public class JsonFileStorage<T> {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final Class<T> type;

  public JsonFileStorage(Class<T> type) {
    this.type = type;
  }

  public void saveListToFile(List<T> data, String filePath) {
    objectMapper.writerWithDefaultPrettyPrinter()
        .writeValue(new File(filePath), data);

  }

  public List<T> loadListFromFile(String filePath) {
    JavaType listType = objectMapper.getTypeFactory()
        .constructCollectionType(List.class, type);
    return objectMapper.readValue(new File(filePath), listType);
  }
}
