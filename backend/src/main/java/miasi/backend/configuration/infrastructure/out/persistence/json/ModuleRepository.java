package miasi.backend.configuration.infrastructure.out.persistence.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import miasi.backend.common.infrastructure.out.persistence.json.JsonFileStorage;
import miasi.backend.configuration.application.port.out.ModuleRepositoryPort;
import miasi.backend.configuration.domain.model.Module;
import miasi.backend.configuration.domain.model.ModuleCatalog;
import miasi.backend.configuration.domain.model.ModuleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ModuleRepository implements ModuleRepositoryPort {
  final ModuleRecordList<Module> modules;
  final ModuleRecordList<ModuleType> types;

  public ModuleRepository(
      @Value("${database.filename.modules}") String filePath1,
      @Value("${database.filename.module.types}") String filePath2)
      throws IOException {
    JsonFileStorage<Module> f1 = new JsonFileStorage<>(Module.class);
    this.modules = new ModuleRecordList<>(this.loadFile(f1, filePath1), filePath1, f1);

    JsonFileStorage<ModuleType> f2 = new JsonFileStorage<>(ModuleType.class);
    this.types = new ModuleRecordList<>(this.loadFile(f2, filePath2), filePath2, f2);
  }

  @Synchronized
  @Override
  public int add(Module module) {
    List<Module> list = modules.getObjects();

    int index =
        IntStream.range(0, list.size())
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
  @Override
  public int add(ModuleType type) {
    List<ModuleType> list = types.getObjects();

    int index =
        IntStream.range(0, list.size())
            .filter(i -> Objects.equals(list.get(i).getName(), type.getName()))
            .findFirst()
            .orElse(-1);

    if (index != -1) {
      list.set(index, type);
    } else {
      list.add(type);
      index = list.size() - 1;
    }

    save();
    return index;
  }

  @Synchronized
  public void save() {
    modules.save();
    types.save();
  }

  @Synchronized
  public List<Module> getModules() {
    return List.copyOf(modules.getObjects());
  }

  @Synchronized
  public List<ModuleType> getModuleTypes() {
    return List.copyOf(types.getObjects());
  }

  private <T> List<T> loadFile(JsonFileStorage<T> database, String fileName) throws IOException {
    List<T> loaded = database.loadListFromFile(fileName);
    return loaded != null ? loaded : new ArrayList<>();
  }

  @Override
  public ModuleCatalog getCatalog() {
    return new ModuleCatalog(getModules(), getModuleTypes());
  }
}
