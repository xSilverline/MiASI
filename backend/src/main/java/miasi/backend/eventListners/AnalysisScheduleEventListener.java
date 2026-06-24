package miasi.backend.eventListners;

import lombok.RequiredArgsConstructor;
import miasi.backend.domains.analysis.AnalysisScheduleEventInbox;
import miasi.backend.events.MissionScheduleCreated;
import miasi.backend.events.MissionScheduleUpdated;
import miasi.backend.events.ModuleStateChangeScheduled;
import miasi.backend.events.SupplyDeliveryScheduled;
import miasi.backend.events.ThreatScheduled;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalysisScheduleEventListener {

  private final AnalysisScheduleEventInbox inbox;

  @EventListener
  public void onMissionScheduleCreated(MissionScheduleCreated event) {
    inbox.record(event);
  }

  @EventListener
  public void onMissionScheduleUpdated(MissionScheduleUpdated event) {
    inbox.record(event);
  }

  @EventListener
  public void onSupplyDeliveryScheduled(SupplyDeliveryScheduled event) {
    inbox.record(event);
  }

  @EventListener
  public void onThreatScheduled(ThreatScheduled event) {
    inbox.record(event);
  }

  @EventListener
  public void onModuleStateChangeScheduled(ModuleStateChangeScheduled event) {
    inbox.record(event);
  }
}
