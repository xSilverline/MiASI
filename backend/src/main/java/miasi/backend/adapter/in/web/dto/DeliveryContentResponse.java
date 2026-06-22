package miasi.backend.adapter.in.web.dto;

import java.util.List;

public record DeliveryContentResponse(List<DeliveryItemResponse> items, double totalWeight) {}
