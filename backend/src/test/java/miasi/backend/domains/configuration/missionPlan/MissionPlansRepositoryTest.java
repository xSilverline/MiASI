package miasi.backend.domains.configuration.missionPlan;


import miasi.backend.domains.configuration.ConfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MissionPlansRepositoryTest {
  @Autowired
  ConfService ctx;

  @Test
  void saveMissionPlan() {
    ctx.saveMissionPlan(new MissionPlan());
  }
}