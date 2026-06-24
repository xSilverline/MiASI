package miasi.backend.configuration.infrastructure.out.persistence.json;

import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import miasi.backend.common.infrastructure.out.persistence.json.JsonFileStorage;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModuleRecordList<T> {
  final List<T> objects;
  final String fileName;
  final JsonFileStorage<T> database;

  public ModuleRecordList(List<T> objects, String fileName, JsonFileStorage<T> database) {
    this.objects = new ArrayList<>(objects);
    this.fileName = fileName;
    this.database = database;
  }

  public void add(T object) {
    objects.add(object);
  }

  public void save() {
    database.saveListToFile(objects, fileName);
  }

  public void remove(T object) {
    objects.remove(object);
  }
}
