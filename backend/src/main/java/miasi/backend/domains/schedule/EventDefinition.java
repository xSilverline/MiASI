package miasi.backend.domains.schedule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.schedule.enums.EventType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class EventDefinition {
  String id;
  String name;
  EventType type;
  String description;
  String affectedElement;
  String consequence;

  public EventDefinition(String id, String name, EventType type, String description) {
    this(id, name, type, description, null, null);
  }

  public EventDefinition(
      String id,
      String name,
      EventType type,
      String description,
      String affectedElement,
      String consequence) {
    this.id = id;
    this.name = name;
    this.type = type;
    this.description = description;
    this.affectedElement = affectedElement;
    this.consequence = consequence;
  }
}
