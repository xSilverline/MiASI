package miasi.backend.domains.analysis.domain.schedule;

import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.modules.Module;
import miasi.backend.domains.analysis.domain.modules.ModuleState;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThreatProcessorTest {

  private final ThreatProcessor processor = new ThreatProcessor();

  @Test
  void shouldDecreaseResourceAfterQuantityThreat() {
    // Given
    List<Resource> warehouse = new ArrayList<>(List.of(
        new Resource(ResourceType.OXYGEN, 100)
    ));
    Threat threat =
        new Threat(
            1,
            5,
            ImpactType.QUANTITY_CHANGE,
            ImpactTarget.RESOURCE,
            "OXYGEN",
            20
        );

    // When
    processor.process(
        2,
        List.of(threat),
        null,
        warehouse
    );

    // Then
    assertThat(warehouse.getFirst().getAmount())
        .isEqualTo(80);
  }


  @Test
  void shouldDestroyModuleAfterStateThreat() {
    // Given
    Module module =
        new Module(
            "1",
            "Engine",
            100,
            0,
            1,
            List.of(),
            List.of(),
            ModuleState.ACTIVE,
            1f
        );
    List<Module> modules = new ArrayList<>(List.of(module));

    Threat threat =
        new Threat(
            1,
            2,
            ImpactType.STATE_CHANGE,
            ImpactTarget.MODULE,
            "Engine",
            0
        );

    // When
    processor.process(
        1,
        List.of(threat),
        modules,
        null
    );

    // Then
    assertThat(modules.getFirst().getStatus())
        .isEqualTo(ModuleState.DESTROYED);
  }
}