package miasi.backend.schedule.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DeliveryItem {
  String itemId;
  DeliveryItemType itemType;
  double quantity;
  double weight;
}
