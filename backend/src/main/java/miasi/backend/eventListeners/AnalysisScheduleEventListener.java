package miasi.backend.eventListeners;

import java.util.function.Consumer;
import miasi.backend.domains.analysis.AnalysisScheduleEventInbox;
import miasi.backend.events.MissionScheduleCreated;
import miasi.backend.events.MissionScheduleUpdated;
import miasi.backend.events.ModuleStateChangeScheduled;
import miasi.backend.events.SupplyDeliveryScheduled;
import miasi.backend.events.ThreatScheduled;
import miasi.backend.sharedkernel.events.IntegrationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AnalysisScheduleEventListener {
  private final AnalysisScheduleEventInbox inbox;
  private final Consumer<IntegrationEvent> processor;

  @Autowired
  public AnalysisScheduleEventListener(AnalysisScheduleEventInbox inbox) {
    this(inbox, ignored -> {});
  }

  AnalysisScheduleEventListener(
      AnalysisScheduleEventInbox inbox, Consumer<IntegrationEvent> processor) {
    this.inbox = inbox;
    this.processor = processor;
  }

  @EventListener
  public void onMissionScheduleCreated(MissionScheduleCreated event) {
    inbox.handle(event, processor);
  }

  @EventListener
  public void onMissionScheduleUpdated(MissionScheduleUpdated event) {
    inbox.handle(event, processor);
  }

  @EventListener
  public void onSupplyDeliveryScheduled(SupplyDeliveryScheduled event) {
    inbox.handle(event, processor);
  }

  @EventListener
  public void onThreatScheduled(ThreatScheduled event) {
    inbox.handle(event, processor);
  }

  @EventListener
  public void onModuleStateChangeScheduled(ModuleStateChangeScheduled event) {
    inbox.handle(event, processor);
  }
}
