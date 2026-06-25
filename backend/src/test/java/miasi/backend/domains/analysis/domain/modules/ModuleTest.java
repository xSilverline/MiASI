package miasi.backend.domains.analysis.domain.modules;


import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class ModuleTest {


  @Test
  void shouldCreateDeepCopyOfModule() {
    // Given
    Module module =
        new Module(
            "M1",
            "Generator",
            100,
            1,
            2,
            List.of(new Resource(ResourceType.OXYGEN, 10)),
            List.of(new Resource(ResourceType.WATER, 5)),
            ModuleState.ACTIVE,
            1f
        );

    // When
    Module copy = module.copy();

    // Then
    assertThat(copy).isNotSameAs(module);
    assertThat(copy.getId()).isEqualTo(module.getId());
    assertThat(copy.getProduction()).isNotSameAs(module.getProduction());
  }
}