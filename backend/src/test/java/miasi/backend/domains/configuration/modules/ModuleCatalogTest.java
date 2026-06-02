package miasi.backend.domains.configuration.modules;

import miasi.backend.domains.configuration.ConfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ModuleCatalogTest {
  @Autowired
  ConfService ctx;

  @Test
  void saveModule() {
    ctx.addModule(new Module());
    ctx.addModuleType(ModuleType.genSample());
  }
}