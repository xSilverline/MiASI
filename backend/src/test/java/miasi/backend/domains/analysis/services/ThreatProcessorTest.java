package miasi.backend.domains.analysis.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Resource resource = new Resource(ResourceType.WATER, 100);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("WATER");
    when(threat.getImpactValue()).thenReturn(20f);

    List<Resource> warehouse = new ArrayList<>(List.of(resource));

    // When
    processor.process(3, List.of(threat), null, warehouse);

    // Then
    assertEquals(80, warehouse.getFirst().getAmount());
  }

  @Test
  void shouldNotDecreaseBelowZero() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Resource resource = new Resource(ResourceType.WATER, 5);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(10);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("WATER");
    when(threat.getImpactValue()).thenReturn(20f);

    List<Resource> warehouse = new ArrayList<>(List.of(resource));

    // When
    processor.process(2, List.of(threat), null, warehouse);

    // Then
    assertEquals(0, warehouse.getFirst().getAmount());
  }

  @Test
  void shouldIgnoreQuantityChangeWhenWarehouseIsNull() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);

    // When
    processor.process(2, List.of(threat), null, null);

    // Then
    assertTrue(true);
  }


  @Test
  void shouldChangeModuleEfficiency() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Module module = mock(Module.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.EFFICIENCY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("DRILL");
    when(threat.getImpactValue()).thenReturn(0.3f);

    when(module.getName()).thenReturn("DRILL");
    when(module.getEfficiency()).thenReturn(0.8f);
    when(module.withEfficiency(anyFloat())).thenReturn(module);

    // When
    processor.process(2, List.of(threat), List.of(module), null);

    // Then
    verify(module).withEfficiency(0.5f);
  }

  @Test
  void shouldNotAllowNegativeEfficiency() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Module module = mock(Module.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.EFFICIENCY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("MODULE");
    when(threat.getImpactValue()).thenReturn(5f);

    when(module.getName()).thenReturn("MODULE");
    when(module.getEfficiency()).thenReturn(1f);

    // When
    processor.process(2, List.of(threat), List.of(module), null);

    // Then
    verify(module).withEfficiency(0f);
  }


  @Test
  void shouldIgnoreEfficiencyChangeWhenModulesNull() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(3);
    when(threat.getType()).thenReturn(ImpactType.EFFICIENCY_CHANGE);

    // When
    processor.process(2, List.of(threat), null, null);

    // Then
    assertTrue(true);
  }

  @Test
  void shouldDestroyModuleForStateChangeThreat() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Module module = mock(Module.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(3);
    when(threat.getType()).thenReturn(ImpactType.STATE_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("ENGINE");
    when(module.getName()).thenReturn("ENGINE");

    // When
    processor.process(2, List.of(threat), List.of(module), null);

    // Then
    verify(module).withStatus(ModuleState.DESTROYED);
    verify(module).withEfficiency(0f);
  }

  @Test
  void shouldIgnoreStateChangeWhenModulesNull() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(3);
    when(threat.getType()).thenReturn(ImpactType.STATE_CHANGE);

    // When
    processor.process(2, List.of(threat), null, null);

    // Then
    assertTrue(true);
  }

  @Test
  void shouldReturnWithoutProcessingWhenThreatsNull() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();

    // When
    processor.process(1, null, null, null);

    // Then
    assertTrue(true);
  }

  @Test
  void shouldIgnoreThreatOutsideActivePeriod() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Module module = mock(Module.class);

    when(threat.getSol()).thenReturn(10);
    when(threat.getDurationSols()).thenReturn(2);

    // When
    processor.process(1, List.of(threat), List.of(module), null);

    // Then
    verifyNoInteractions(module);
  }

  @Test
  void shouldIgnoreResourceWhenIdentifierDoesNotMatch() {
    // Given
    ThreatProcessor processor = new ThreatProcessor();
    Threat threat = mock(Threat.class);
    Resource resource = new Resource(ResourceType.WATER, 50);

    when(threat.getSol()).thenReturn(1);
    when(threat.getDurationSols()).thenReturn(5);
    when(threat.getType()).thenReturn(ImpactType.QUANTITY_CHANGE);
    when(threat.getTargetIdentifier()).thenReturn("OXYGEN");
    when(threat.getImpactValue()).thenReturn(20f);

    List<Resource> warehouse = new ArrayList<>(List.of(resource));

    // When
    processor.process(2, List.of(threat), null, warehouse);

    // Then
    assertEquals(50, warehouse.getFirst().getAmount());
  }
}