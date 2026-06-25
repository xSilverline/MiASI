package miasi.backend.domains.analysis.domain._simulation;

import miasi.backend.domains.analysis.domain.core.DailyBalance;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.crew.ConsumptionMode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SimulationOutcomeEvaluatorTest {

  private final SimulationOutcomeEvaluator evaluator =
      new SimulationOutcomeEvaluator();

  @Test
  void shouldCalculateMinimumNegativeResourcesAsRequiredSupplies() {
    // Given
    DailyState day1 = createState(-10, -5, -3);
    DailyState day2 = createState(-20, -2, -8);

    // When
    List<Resource> result = evaluator.calculateMinimumSurvivalSupplies(
        List.of(day1, day2)
    );

    // Then
    assertThat(result)
        .extracting(Resource::getAmount)
        .containsExactly(20f, 5f, 8f);
  }


  @Test
  void shouldReturnZeroWhenNoDeficitExists() {
    // Given
    DailyState state = createState(5, 10, 3);

    // When
    List<Resource> result = evaluator.calculateMinimumSurvivalSupplies(List.of(state));

    // then
    assertThat(result)
        .extracting(Resource::getAmount)
        .containsExactly(0f, 0f, 0f);
  }

  private DailyState createState(
      float oxygen,
      float water,
      float food
  ) {

    return new DailyState(
        12,
        List.of(
            new Resource(ResourceType.OXYGEN, oxygen),
            new Resource(ResourceType.WATER, water),
            new Resource(ResourceType.FOOD, food)
        ), new DailyBalance(),
        ConsumptionMode.MINIMAL,
        List.of(),
        Set.of()
    );
  }
}