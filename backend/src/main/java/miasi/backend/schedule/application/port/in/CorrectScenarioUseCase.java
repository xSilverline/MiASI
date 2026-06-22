package miasi.backend.schedule.application.port.in;

import miasi.backend.schedule.domain.model.ScenarioDraft;
import miasi.backend.schedule.domain.model.ScheduledEvent;

public interface CorrectScenarioUseCase {
  ScenarioDraft correctScenarioEvent(String draftId, String eventId, ScheduledEvent correctedEvent);
}
