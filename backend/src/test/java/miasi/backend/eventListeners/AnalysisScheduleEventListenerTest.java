package miasi.backend.eventListeners;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import miasi.backend.domains.analysis.AnalysisScheduleEventInbox;
import miasi.backend.events.MissionScheduleCreated;
import miasi.backend.events.MissionScheduleUpdated;
import miasi.backend.events.ModuleStateChangeScheduled;
import miasi.backend.sharedkernel.model.ModuleState;
import org.junit.jupiter.api.Test;

class AnalysisScheduleEventListenerTest {
  @Test
  void shouldRecordScheduleEventsForAnalysis() {
    AnalysisScheduleEventInbox inbox = new AnalysisScheduleEventInbox();
    AnalysisScheduleEventListener listener = new AnalysisScheduleEventListener(inbox);
    MissionScheduleCreated created = MissionScheduleCreated.create("schedule-1", "plan-1");
    MissionScheduleUpdated updated = MissionScheduleUpdated.create("schedule-1");
    ModuleStateChangeScheduled stateChange =
        ModuleStateChangeScheduled.create("schedule-1", 12, "habitat-1", ModuleState.DESTROYED);

    listener.onMissionScheduleCreated(created);
    listener.onMissionScheduleUpdated(updated);
    listener.onModuleStateChangeScheduled(stateChange);

    assertEquals(3, inbox.getReceivedEvents().size());
    assertTrue(inbox.getReceivedEvents().contains(created));
    assertTrue(inbox.getReceivedEvents().contains(updated));
    assertTrue(inbox.getReceivedEvents().contains(stateChange));
  }
}
