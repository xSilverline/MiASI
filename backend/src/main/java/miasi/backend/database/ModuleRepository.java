package miasi.backend.database;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.modules.ModuleType;
import miasi.backend.domains.configuration.ports.IModuleRepositoryPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ModuleRepository implements IModuleRepositoryPort {
  final ModuleRecordList<Module> modules;
  final ModuleRecordList<ModuleType> types;

  public ModuleRepository(
      @Value("${database.filename.modules}") String filePath1,
      @Value("${database.filename.module.types}") String filePath2
  ) throws IOException {
    JsonFileStorage f1 = new JsonFileStorage();
    this.modules = new ModuleRecordList<>(
        this.loadFile(f1, filePath1, new TypeReference<>() {
        }),
        filePath1,
        f1
    );

    JsonFileStorage f2 = new JsonFileStorage();
    this.types = new ModuleRecordList<>(
        this.loadFile(f2, filePath2, new TypeReference<>() {
        }),
        filePath2,
        f2
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
  @Override
  public int add(ModuleType type) {
    List<ModuleType> list = types.getObjects();

    int index = IntStream.range(0, list.size())
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

  private <T> List<T> loadFile(
      JsonFileStorage database,
      String fileName,
      TypeReference<List<T>> type
  ) throws IOException {

    List<T> loaded = database.loadFromFile(fileName, type);
    return loaded != null ? loaded : new ArrayList<>();
  }

  public ModuleCatalog toJson() {
    return new ModuleCatalog(getModules(), getModuleTypes());
  }
}
