package miasi.backend.api.jsons;

import java.util.List;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.enums.EventType;

public record EventDefinitionsCatalogResponse(
    List<EventDefinition> deliveries, List<EventDefinition> threats) {

  public static EventDefinitionsCatalogResponse from(List<EventDefinition> definitions) {
    List<EventDefinition> safeDefinitions = definitions == null ? List.of() : definitions;
    return new EventDefinitionsCatalogResponse(
        filterByType(safeDefinitions, EventType.SUPPLY_DELIVERY),
        filterByType(safeDefinitions, EventType.THREAT));
  }

  private static List<EventDefinition> filterByType(
      List<EventDefinition> definitions, EventType type) {
    return definitions.stream().filter(definition -> type.equals(definition.getType())).toList();
  }
}
