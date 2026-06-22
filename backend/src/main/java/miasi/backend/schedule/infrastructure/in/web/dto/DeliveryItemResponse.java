package miasi.backend.schedule.infrastructure.in.web.dto;

import miasi.backend.schedule.domain.model.DeliveryItemType;

public record DeliveryItemResponse(
    String itemId, DeliveryItemType itemType, double quantity, double weight) {}
