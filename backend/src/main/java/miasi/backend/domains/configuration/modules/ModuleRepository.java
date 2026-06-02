package miasi.backend.domains.configuration.modules;

import lombok.AccessLevel;
import lombok.Synchronized;
import lombok.experimental.FieldDefaults;
import miasi.backend.database.JsonFileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class ModuleRepository {
  final ModuleRecordList<Module> modules;
  final ModuleRecordList<ModuleType> types;

  public ModuleRepository(
      @Value("${database.filename.modules}") String filePath1,
      @Value("${database.filename.module.types}") String filePath2
  ) throws IOException {
    JsonFileStorage f1 = new JsonFileStorage();
    this.modules = new ModuleRecordList<>(
        this.loadFile(f1, filePath1, new TypeReference<List<Module>>() {
        }),
        filePath1,
        f1
    );

    JsonFileStorage f2 = new JsonFileStorage();
    this.types = new ModuleRecordList<>(
        this.loadFile(f2, filePath2, new TypeReference<List<ModuleType>>() {
        }),
        filePath2,
        f2
    );
  }

  @Synchronized
  public int add(Module module) {
    modules.add(module);
    save();
    return modules.getObjects().size() - 1;
  }

  @Synchronized
  public int add(ModuleType type) {
    types.add(type);
    save();
    return types.getObjects().size() - 1;
  }

  @Synchronized
  public void remove(Module module) {
    modules.remove(module);
    save();
  }

  @Synchronized
  public void remove(ModuleType moduleType) {
    types.remove(moduleType);
    save();
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
