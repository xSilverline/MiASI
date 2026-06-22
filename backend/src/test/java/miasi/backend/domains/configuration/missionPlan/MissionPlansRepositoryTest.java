package miasi.backend.domains.configuration.missionPlan;

import miasi.backend.configuration.application.ConfigurationApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MissionPlansRepositoryTest {
  @Autowired ConfigurationApplicationService ctx;

  @Test
  void saveMissionPlan() {
    ctx.saveMissionPlan(new MissionPlan());
  }
}
