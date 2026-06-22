package miasi.backend.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import miasi.backend.domains.schedule.DeliveryItem;
import miasi.backend.schedule.domain.DeliveryItemType;

public record DeliveryItemRequest(
    @NotBlank(message = "Delivery item id is required") String itemId,
    @NotNull(message = "Delivery item type is required") DeliveryItemType itemType,
    @PositiveOrZero(message = "Delivery item quantity cannot be negative") double quantity,
    @PositiveOrZero(message = "Delivery item weight cannot be negative") double weight) {

  public DeliveryItem toDomain() {
    return new DeliveryItem(itemId, itemType, quantity, weight);
  }
}
