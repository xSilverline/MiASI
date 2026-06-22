package miasi.backend.domains.configuration.modules;

import miasi.backend.configuration.application.ConfigurationApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ModuleCatalogTest {
  @Autowired ConfigurationApplicationService ctx;

  @Test
  void saveModule() {
    ctx.addModule(new Module());
    ctx.addModuleType(ModuleType.genSample());
  }
}
