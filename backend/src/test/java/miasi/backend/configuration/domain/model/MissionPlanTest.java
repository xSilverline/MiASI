package miasi.backend.configuration.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MissionPlanTest {

  private MissionPlan missionPlan;

  @BeforeEach
  void setUp() {
    // given - before test
    missionPlan = new MissionPlan();
  }

  @Test
  void addSexProfile_shouldAddProfileToCrew() {
    // given
    int initialSize = missionPlan.getCrew().size();
    SexProfile profile = new SexProfile();

    // when
    missionPlan.AddSexProfile(profile);

    // then
    assertEquals(initialSize + 1, missionPlan.getCrew().size());
    assertTrue(missionPlan.getCrew().contains(profile));
  }

  @Test
  void addModule_shouldAddModuleToList() {
    // given
    int initialSize = missionPlan.getModules().size();
    Module module = new Module();

    // when
    missionPlan.addModule(module);

    // then
    assertEquals(initialSize + 1, missionPlan.getModules().size());
    assertTrue(missionPlan.getModules().contains(module));
  }

  @Test
  void constructor_shouldInitializeDefaultValues() {
    // then
    assertNotNull(missionPlan.getCrew());
    assertNotNull(missionPlan.getModules());
    assertNotNull(missionPlan.getStartingResources());

    assertFalse(missionPlan.getCrew().isEmpty());
    assertFalse(missionPlan.getModules().isEmpty());
    assertEquals(0, missionPlan.getMissionDurationSols());
    assertEquals(0f, missionPlan.getMaxStartingWeight());
  }
}
