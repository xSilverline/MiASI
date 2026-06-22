package miasi.backend.database;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.ports.IModuleRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ModuleRepository implements IModuleRepositoryPort {
  final ModuleRecordList<Module> modules;

  public ModuleRepository(
      @Value("${database.filename.modules}") String filePath1
  ) throws IOException {
    JsonFileStorage<Module> f1 = new JsonFileStorage<>(Module.class);
    this.modules = new ModuleRecordList<>(
        this.loadFile(f1, filePath1),
        filePath1,
        f1
    );
  }

  @Synchronized
  @Override
  public int add(Module module) {
    List<Module> list = modules.getObjects();

    int index = IntStream.range(0, list.size())
        .filter(i -> Objects.equals(list.get(i).getName(), module.getName()))
        .findFirst()
        .orElse(-1);

    if (index != -1) {
      list.set(index, module);
    } else {
      list.add(module);
      index = list.size() - 1;
    }

    save();
    return index;
  }


  @Synchronized
  public void save() {
    modules.save();
  }

  @Synchronized
  public List<Module> getModules() {
    return List.copyOf(modules.getObjects());
  }

  private <T> List<T> loadFile(
      JsonFileStorage<T> database,
      String fileName
  ) throws IOException {

    List<T> loaded = database.loadListFromFile(fileName);
    return loaded != null ? loaded : new ArrayList<>();
  }

  public List<Module> toJson() {
    return getModules();
  }
}
