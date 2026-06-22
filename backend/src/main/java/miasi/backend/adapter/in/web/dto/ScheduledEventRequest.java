package miasi.backend.adapter.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import miasi.backend.domains.schedule.DeliveryContent;
import miasi.backend.domains.schedule.ModuleStateChange;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.SupplyDelivery;
import miasi.backend.domains.schedule.Threat;
import miasi.backend.schedule.domain.EventType;
import miasi.backend.schedule.domain.ThreatType;
import miasi.backend.sharedkernel.model.ModuleState;

public record ScheduledEventRequest(
    @NotBlank(message = "Scheduled event id is required") String id,
    @NotNull(message = "Event type is required") EventType type,
    @Min(value = 1, message = "Event sol must be at least 1") int sol,
    @NotBlank(message = "Description is required") String description,
    ThreatType threatType,
    String affectedElement,
    @PositiveOrZero(message = "Threat impact value cannot be negative") Double impactValue,
    @Min(value = 1, message = "Threat duration must be at least 1 sol") Integer durationSols,
    String impactUnit,
    @Valid DeliveryContentRequest content,
    String moduleId,
    ModuleState newState) {

  public ScheduledEvent toDomain() {
    return switch (requireType()) {
      case THREAT -> toThreat();
      case SUPPLY_DELIVERY -> toSupplyDelivery();
      case MODULE_STATE_CHANGE -> toModuleStateChange();
    };
  }

  private EventType requireType() {
    if (type == null) {
      throw new IllegalArgumentException("Event type is required");
    }
    return type;
  }

  private Threat toThreat() {
    Threat threat =
        new Threat(
            requireValue(threatType, "Threat type is required"),
            requireText(affectedElement, "Affected element is required"),
            requireValue(impactValue, "Threat impact value is required"),
            requireValue(durationSols, "Threat duration is required"),
            requireText(impactUnit, "Threat impact unit is required"));
    applyBaseFields(threat);
    return threat;
  }

  private SupplyDelivery toSupplyDelivery() {
    DeliveryContent deliveryContent =
        requireValue(content, "Delivery content is required").toDomain();
    SupplyDelivery delivery = new SupplyDelivery(deliveryContent);
    applyBaseFields(delivery);
    return delivery;
  }

  private ModuleStateChange toModuleStateChange() {
    ModuleStateChange stateChange =
        new ModuleStateChange(
            requireText(moduleId, "Module id is required"),
            requireValue(newState, "New module state is required"));
    applyBaseFields(stateChange);
    return stateChange;
  }

  private void applyBaseFields(ScheduledEvent event) {
    event.setId(id);
    event.setType(type);
    event.setSol(sol);
    event.setDescription(description);
  }

  private static String requireText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  private static <T> T requireValue(T value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }
}
