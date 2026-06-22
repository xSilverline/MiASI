package miasi.backend.schedule.infrastructure.in.web.dto;

import java.util.List;
import miasi.backend.schedule.domain.model.DeliveryContent;
import miasi.backend.schedule.domain.model.DeliveryItem;
import miasi.backend.schedule.domain.model.MissionSchedule;
import miasi.backend.schedule.domain.model.MissionTimeline;
import miasi.backend.schedule.domain.model.ModuleStateChange;
import miasi.backend.schedule.domain.model.ScenarioDraft;
import miasi.backend.schedule.domain.model.ScheduledEvent;
import miasi.backend.schedule.domain.model.SupplyDelivery;
import miasi.backend.schedule.domain.model.Threat;

public final class ScheduleResponseMapper {

  private ScheduleResponseMapper() {}

  public static ScheduleResponse toResponse(MissionSchedule schedule) {
    return new ScheduleResponse(
        schedule.getId(),
        schedule.getMissionPlanId(),
        schedule.getDurationSols(),
        schedule.getStatus(),
        toEventResponses(schedule.getEvents()));
  }

  public static TimelineResponse toResponse(MissionTimeline timeline) {
    return new TimelineResponse(toEventResponses(timeline.getEventsSortedBySol()));
  }

  public static ScenarioDraftResponse toResponse(ScenarioDraft draft) {
    return new ScenarioDraftResponse(
        draft.getId(),
        draft.getMissionPlanId(),
        draft.getDurationSols(),
        draft.getMode(),
        draft.getDifficulty(),
        toEventResponses(draft.getProposedEvents()));
  }

  private static List<ScheduledEventResponse> toEventResponses(List<ScheduledEvent> events) {
    if (events == null) {
      return List.of();
    }
    return events.stream().map(ScheduleResponseMapper::toEventResponse).toList();
  }

  private static ScheduledEventResponse toEventResponse(ScheduledEvent event) {
    Threat threat = event instanceof Threat value ? value : null;
    SupplyDelivery delivery = event instanceof SupplyDelivery value ? value : null;
    ModuleStateChange stateChange = event instanceof ModuleStateChange value ? value : null;

    return new ScheduledEventResponse(
        event.getId(),
        event.getType(),
        event.getSol(),
        event.getDescription(),
        threat == null ? null : threat.getThreatType(),
        threat == null ? null : threat.getAffectedElement(),
        threat == null ? null : threat.getImpactValue(),
        threat == null ? null : threat.getDurationSols(),
        threat == null ? null : threat.getImpactUnit(),
        delivery == null ? null : toDeliveryContentResponse(delivery.getContent()),
        stateChange == null ? null : stateChange.getModuleId(),
        stateChange == null ? null : stateChange.getNewState());
  }

  private static DeliveryContentResponse toDeliveryContentResponse(DeliveryContent content) {
    if (content == null) {
      return null;
    }
    return new DeliveryContentResponse(
        toDeliveryItemResponses(content.getItems()), content.getTotalWeight());
  }

  private static List<DeliveryItemResponse> toDeliveryItemResponses(List<DeliveryItem> items) {
    if (items == null) {
      return List.of();
    }
    return items.stream()
        .map(
            item ->
                new DeliveryItemResponse(
                    item.getItemId(), item.getItemType(), item.getQuantity(), item.getWeight()))
        .toList();
  }
}
