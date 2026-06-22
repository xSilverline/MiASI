package miasi.backend.schedule.infrastructure.in.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import miasi.backend.schedule.domain.model.DeliveryContent;

public record DeliveryContentRequest(
    @NotNull(message = "Delivery items are required") List<@Valid DeliveryItemRequest> items,
    @NotNull(message = "Delivery total weight is required")
        @PositiveOrZero(message = "Delivery total weight cannot be negative")
        Double totalWeight) {

  public DeliveryContent toDomain() {
    return new DeliveryContent(
        items == null ? List.of() : items.stream().map(DeliveryItemRequest::toDomain).toList(),
        totalWeight == null ? 0.0 : totalWeight);
  }
}
