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
    File parent = file.getParentFile();
    if (parent != null) {
      parent.mkdirs();
    }

    objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
  }

  public List<T> loadListFromFile(String filePath) {
    JavaType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, type);
    return objectMapper.readValue(new File(filePath), listType);
  }
}
