package miasi.backend.eventListners;

import static org.junit.jupiter.api.Assertions.assertEquals;

import miasi.backend.domains.analisis.AnalysisScheduleEventInbox;
import miasi.backend.events.MissionScheduleCreated;
import miasi.backend.events.MissionScheduleUpdated;
import miasi.backend.events.ModuleStateChangeScheduled;
import miasi.backend.enums.ModuleState;
import org.junit.jupiter.api.Test;

class AnalysisScheduleEventListenerTest {
  @Test
  void shouldRecordScheduleEventsForAnalysis() {
    AnalysisScheduleEventInbox inbox = new AnalysisScheduleEventInbox();
    AnalysisScheduleEventListener listener = new AnalysisScheduleEventListener(inbox);
    MissionScheduleCreated created = new MissionScheduleCreated("schedule-1", "plan-1");
    MissionScheduleUpdated updated = new MissionScheduleUpdated("schedule-1");
    ModuleStateChangeScheduled stateChange =
        new ModuleStateChangeScheduled("schedule-1", 12, "habitat-1", ModuleState.DESTROYED);

    listener.onMissionScheduleCreated(created);
    listener.onMissionScheduleUpdated(updated);
    listener.onModuleStateChangeScheduled(stateChange);

    assertEquals(3, inbox.getReceivedEvents().size());
    assertEquals(created, inbox.getReceivedEvents().get(0));
    assertEquals(updated, inbox.getReceivedEvents().get(1));
    assertEquals(stateChange, inbox.getReceivedEvents().get(2));
  }
}
