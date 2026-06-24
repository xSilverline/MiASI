package miasi.backend.domains.analysis.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import miasi.backend.domains.analysis.domain.crew.DemandCalculator;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.crew.ConsumptionMode;
import miasi.backend.domains.analysis.domain.crew.CrewGroup;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import org.junit.jupiter.api.Test;

class DemandCalculatorTest {

  @Test
  void shouldCalculateCrewDemand() {
    // Given
    DemandCalculator calculator = new DemandCalculator();
    CrewGroup group = mock(CrewGroup.class);

    when(group.getCount()).thenReturn(2);
    when(group.getDailyDemand(any())).thenReturn(
        List.of(new Resource(ResourceType.FOOD, 5))
    );

    // When
    List<Resource> result = calculator.calculateCrewDemand(
        List.of(group),
        ConsumptionMode.OPTIMAL
    );

    // Then
    assertEquals(1, result.size());
    assertEquals(
        10,
        result.getFirst().getAmount()
    );
  }

  @Test
  void shouldCalculateActiveModulesDemand() {
    // Given
    DemandCalculator calculator = new DemandCalculator();
    Module module = mock(Module.class);
    when(module.getStatus()).thenReturn(ModuleState.ACTIVE);
    when(module.getEfficiency()).thenReturn(0.5f);
    when(module.getConsumption()).thenReturn(
        List.of(
            new Resource(
                ResourceType.WATER,
                10
            )
        )
    );

    // When
    List<Resource> result = calculator.calculateModulesDemand(
        List.of(module)
    );

    // Then
    assertEquals(5, result.getFirst().getAmount());
  }
}
