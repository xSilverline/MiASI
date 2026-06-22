package miasi.backend.analysis.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import miasi.backend.analysis.domain.model.core.Resource;
import miasi.backend.analysis.domain.model.crew.ConsumptionMode;
import miasi.backend.analysis.domain.model.crew.CrewGroup;
import miasi.backend.analysis.domain.model.modules.Module;
import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.common.domain.model.ResourceType;
import org.junit.jupiter.api.Test;

class DemandCalculatorTest {

  @Test
  void shouldCalculateCrewDemand() {
    // Given
    DemandCalculator calculator = new DemandCalculator();
    CrewGroup group = mock(CrewGroup.class);

    when(group.getCount()).thenReturn(2);
    when(group.getDailyDemand(any())).thenReturn(List.of(new Resource(ResourceType.FOOD, 5)));

    // When
    List<Resource> result = calculator.calculateCrewDemand(List.of(group), ConsumptionMode.OPTIMAL);

    // Then
    assertEquals(1, result.size());
    assertEquals(10, result.getFirst().getAmount());
  }

  @Test
  void shouldCalculateActiveModulesDemand() {
    // Given
    DemandCalculator calculator = new DemandCalculator();
    Module module = mock(Module.class);
    when(module.getStatus()).thenReturn(ModuleState.ACTIVE);
    when(module.getEfficiency()).thenReturn(0.5f);
    when(module.getConsumption()).thenReturn(List.of(new Resource(ResourceType.WATER, 10)));

    // When
    List<Resource> result = calculator.calculateModulesDemand(List.of(module));

    // Then
    assertEquals(5, result.getFirst().getAmount());
  }
}
