package miasi.backend.analysis.infrastructure.in.event;

import java.util.function.Consumer;
import miasi.backend.analysis.domain.model.AnalysisScheduleEventInbox;
import miasi.backend.common.domain.model.event.IntegrationEvent;
import miasi.backend.common.domain.model.event.MissionScheduleCreated;
import miasi.backend.common.domain.model.event.MissionScheduleUpdated;
import miasi.backend.common.domain.model.event.ModuleStateChangeScheduled;
import miasi.backend.common.domain.model.event.SupplyDeliveryScheduled;
import miasi.backend.common.domain.model.event.ThreatScheduled;
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

  public AnalysisScheduleEventListener(
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
