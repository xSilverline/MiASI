package schedule.domain.events;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import schedule.domain.DeliveryContent;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class SupplyDeliveryScheduled {
  String scheduleId;
  int sol;
  DeliveryContent content;
}
