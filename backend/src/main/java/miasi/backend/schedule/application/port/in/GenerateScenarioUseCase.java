package miasi.backend.schedule.application.port.in;

import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.schedule.domain.DifficultyLevel;

public interface GenerateScenarioUseCase {
  ScenarioDraft generateScenario(
      String missionPlanId, int durationSols, DifficultyLevel difficulty);

  ScenarioDraft getScenarioDraft(String draftId);
}
