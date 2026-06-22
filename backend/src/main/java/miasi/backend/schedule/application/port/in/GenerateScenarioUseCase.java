package miasi.backend.schedule.application.port.in;

import miasi.backend.schedule.domain.model.DifficultyLevel;
import miasi.backend.schedule.domain.model.ScenarioDraft;

public interface GenerateScenarioUseCase {
  ScenarioDraft generateScenario(
      String missionPlanId, int durationSols, DifficultyLevel difficulty);

  ScenarioDraft getScenarioDraft(String draftId);
}
