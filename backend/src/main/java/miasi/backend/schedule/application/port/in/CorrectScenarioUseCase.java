package miasi.backend.schedule.application.port.in;

import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.domains.schedule.ScheduledEvent;

public interface CorrectScenarioUseCase {
  ScenarioDraft correctScenarioEvent(String draftId, String eventId, ScheduledEvent correctedEvent);
}
