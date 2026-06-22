package miasi.backend.adapter.in.web.dto;

import miasi.backend.schedule.domain.DeliveryItemType;

public record DeliveryItemResponse(
    String itemId, DeliveryItemType itemType, double quantity, double weight) {}
