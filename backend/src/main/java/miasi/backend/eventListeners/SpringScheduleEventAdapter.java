package miasi.backend.eventListeners;

import java.util.List;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.schedule.DeliveryContent;
import miasi.backend.domains.schedule.DeliveryItem;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ModuleStateChange;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.SupplyDelivery;
import miasi.backend.domains.schedule.Threat;
import miasi.backend.events.MissionScheduleCreated;
import miasi.backend.events.MissionScheduleUpdated;
import miasi.backend.events.ModuleStateChangeScheduled;
import miasi.backend.events.SupplyDeliveryScheduled;
import miasi.backend.events.ThreatScheduled;
import miasi.backend.schedule.application.port.out.ScheduleEventPublisherPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpringScheduleEventAdapter implements ScheduleEventPublisherPort {
  private final ApplicationEventPublisher applicationEventPublisher;

  @Override
  public void publishScheduleCreated(MissionSchedule schedule) {
    applicationEventPublisher.publishEvent(
        MissionScheduleCreated.create(schedule.getId(), schedule.getMissionPlanId()));
  }

  @Override
  public void publishScheduleUpdated(MissionSchedule schedule) {
    applicationEventPublisher.publishEvent(MissionScheduleUpdated.create(schedule.getId()));
  }

  @Override
  public void publishScheduledEventAdded(String scheduleId, ScheduledEvent event) {
    if (event instanceof Threat threat) {
      publishThreat(scheduleId, threat);
      return;
    }
    if (event instanceof SupplyDelivery delivery) {
      publishDelivery(scheduleId, delivery);
      return;
    }
    if (event instanceof ModuleStateChange stateChange) {
      applicationEventPublisher.publishEvent(
          ModuleStateChangeScheduled.create(
              scheduleId,
              stateChange.getSol(),
              stateChange.getModuleId(),
              stateChange.getNewState()));
    }
  }

  private void publishThreat(String scheduleId, Threat threat) {
    applicationEventPublisher.publishEvent(
        ThreatScheduled.create(
            scheduleId,
            threat.getSol(),
            threat.getThreatType(),
            threat.getAffectedElement(),
            threat.getImpactValue(),
            threat.getDurationSols(),
            threat.getImpactUnit()));
  }

  private void publishDelivery(String scheduleId, SupplyDelivery delivery) {
    DeliveryContent content = delivery.getContent();
    List<SupplyDeliveryScheduled.DeliveryItemSnapshot> items =
        content == null || content.getItems() == null
            ? List.of()
            : content.getItems().stream().map(this::toSnapshot).toList();
    applicationEventPublisher.publishEvent(
        SupplyDeliveryScheduled.create(
            scheduleId,
            delivery.getSol(),
            items,
            content == null ? 0.0 : content.getTotalWeight()));
  }

  private SupplyDeliveryScheduled.DeliveryItemSnapshot toSnapshot(DeliveryItem item) {
    return new SupplyDeliveryScheduled.DeliveryItemSnapshot(
        item.getItemId(), item.getItemType(), item.getQuantity(), item.getWeight());
  }
}
