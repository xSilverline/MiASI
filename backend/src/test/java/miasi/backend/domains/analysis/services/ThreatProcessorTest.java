package miasi.backend.domains.analysis.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import miasi.backend.domains.analysis.domain.schedule.ImpactType;
import miasi.backend.domains.analysis.domain.schedule.Threat;
import miasi.backend.domains.analysis.domain.schedule.ThreatProcessor;
import org.junit.jupiter.api.Test;

class ThreatProcessorTest {

  @Test
  void shouldDecreaseResourceAmountForQuantityChangeThreat() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Resource resource = new Resource(ResourceType.WATER, 100);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("WATER");
    when(threat.getImpactValue()).thenReturn(20f);

    List<Resource> warehouse = new ArrayList<>(List.of(resource));

    processor.process(3, List.of(threat), null, warehouse);

    assertEquals(80, warehouse.getFirst().getAmount());
  }

  @Test
  void shouldNotDecreaseBelowZero() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Resource resource = new Resource(ResourceType.WATER, 5);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(10);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("WATER");
    when(threat.getImpactValue()).thenReturn(20f);

    List<Resource> warehouse = new ArrayList<>(List.of(resource));

    processor.process(2, List.of(threat), null, warehouse);

    assertEquals(0, warehouse.getFirst().getAmount());
  }

  @Test
  void shouldIgnoreQuantityChangeWhenWarehouseIsNull() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);

    processor.process(2, List.of(threat), null, null);

    assertTrue(true);
  }

  @Test
  void shouldChangeModuleEfficiency() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    // Używamy Buildera i PRAWDZIWEGO obiektu!
    Module module =
        Module.builder().name("DRILL").efficiency(0.8f).status(ModuleState.ACTIVE).build();

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.EFFICIENCY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("DRILL");
    when(threat.getImpactValue()).thenReturn(0.3f);

    // MUTOWALNA LISTA
    List<Module> modules = new ArrayList<>(List.of(module));

    processor.process(2, List.of(threat), modules, null);

    // Sprawdzamy stan końcowy
    assertEquals(0.5f, modules.get(0).getEfficiency(), 0.01f);
  }

  @Test
  void shouldNotAllowNegativeEfficiency() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    Module module =
        Module.builder().name("MODULE").efficiency(1f).status(ModuleState.ACTIVE).build();

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.EFFICIENCY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("MODULE");
    when(threat.getImpactValue()).thenReturn(5f);

    List<Module> modules = new ArrayList<>(List.of(module));

    processor.process(2, List.of(threat), modules, null);

    assertEquals(0f, modules.get(0).getEfficiency());
  }

  @Test
  void shouldIgnoreEfficiencyChangeWhenModulesNull() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(3);
    when(threat.getType()).thenReturn(ImpactType.EFFICIENCY_CHANGE);

    processor.process(2, List.of(threat), null, null);

    assertTrue(true);
  }

  @Test
  void shouldDestroyModuleForStateChangeThreat() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    Module module =
        Module.builder().name("ENGINE").efficiency(1.0f).status(ModuleState.ACTIVE).build();

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(3);
    when(threat.getType()).thenReturn(ImpactType.STATE_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("ENGINE");

    List<Module> modules = new ArrayList<>(List.of(module));

    processor.process(2, List.of(threat), modules, null);

    assertEquals(ModuleState.DESTROYED, modules.get(0).getStatus());
    assertEquals(0f, modules.get(0).getEfficiency());
  }

  @Test
  void shouldIgnoreStateChangeWhenModulesNull() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(3);
    when(threat.getType()).thenReturn(ImpactType.STATE_CHANGE);

    processor.process(2, List.of(threat), null, null);

    assertTrue(true);
  }

  @Test
  void shouldReturnWithoutProcessingWhenThreatsNull() {
    ThreatProcessor processor = new ThreatProcessor();
    processor.process(1, null, null, null);
    assertTrue(true);
  }

  @Test
  void shouldIgnoreThreatOutsideActivePeriod() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    Module module =
        Module.builder().name("ENGINE").efficiency(1.0f).status(ModuleState.ACTIVE).build();

    when(threat.getSol()).thenReturn(10);
    when(threat.getDurationSols()).thenReturn(2);

    List<Module> modules = new ArrayList<>(List.of(module));

    processor.process(1, List.of(threat), modules, null);

    assertEquals(ModuleState.ACTIVE, modules.get(0).getStatus());
  }

  @Test
  void shouldIgnoreResourceWhenIdentifierDoesNotMatch() {
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Resource resource = new Resource(ResourceType.WATER, 50);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("OXYGEN");
    when(threat.getImpactValue()).thenReturn(20f);

    List<Resource> warehouse = new ArrayList<>(List.of(resource));

    processor.process(2, List.of(threat), null, warehouse);

    assertEquals(50, warehouse.getFirst().getAmount());
  }
}
