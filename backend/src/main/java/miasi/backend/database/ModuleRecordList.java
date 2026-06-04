package miasi.backend.database;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ModuleRecordList<T> {
  final List<T> objects;
  final String fileName;
  final JsonFileStorage database;

  public ModuleRecordList(List<T> objects, String fileName, JsonFileStorage database) {
    this.objects = new ArrayList<>(objects);
    this.fileName = fileName;
    this.database = database;
  }

  public void add(T object) {
    objects.add(object);
  }

  public void save() {
    database.saveToFile(objects, fileName);
  }

  public void remove(T object) {
    objects.remove(object);
  }
}
