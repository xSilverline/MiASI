package miasi.backend.domains.analysis.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import miasi.backend.domains.analysis.domain._simulation.SimulationOutcome;
import miasi.backend.domains.analysis.domain._simulation.SimulationOutcomeEvaluator;
import miasi.backend.domains.analysis.domain._simulation.TimelineSimulator;
import miasi.backend.domains.analysis.domain.core.DailyState;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.core.ObservationType;
import miasi.backend.domains.analysis.domain.core.Resource;
import miasi.backend.domains.analysis.domain.core.ResourceType;
import miasi.backend.domains.analysis.domain.core.Status;
import miasi.backend.domains.analysis.domain.crew.ConsumptionMode;
import miasi.backend.domains.analysis.domain.crew.DemandCalculator;
import miasi.backend.domains.analysis.domain.crew.SurvivalPredictor;
import miasi.backend.domains.analysis.domain.energy.PowerGridSimulator;
import miasi.backend.domains.analysis.domain.modules.ProductionCalculator;
import miasi.backend.domains.analysis.domain.schedule.Delivery;
import miasi.backend.domains.analysis.domain.schedule.DeliveryProcessor;
import miasi.backend.domains.analysis.domain.schedule.ThreatProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TimelineSimulatorTest {

  // Mock all processors and calculators
  @Mock private DemandCalculator demandCalculator;
  @Mock private ProductionCalculator productionCalculator;
  @Mock private DeliveryProcessor deliveryProcessor;
  @Mock private ThreatProcessor threatProcessor;
  @Mock private PowerGridSimulator powerGridSimulator;
  @Mock private SurvivalPredictor survivalPredictor;

  @InjectMocks private TimelineSimulator timelineSimulator;

  private MissionManifest mockManifest;

  @BeforeEach
  void setUp() {
    // Mission: 5 days main + 2 days rescue = 7 days target
    mockManifest =
        new MissionManifest(
            0, 5, 2, 20000f, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

    lenient()
        .when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(new ArrayList<>());
    lenient().when(demandCalculator.calculateModulesDemand(any())).thenReturn(new ArrayList<>());
    lenient()
        .when(demandCalculator.calculateCrewDemand(any(), any()))
        .thenReturn(new ArrayList<>());

    lenient()
        .when(
            survivalPredictor.evaluateCrewConsumptionMode(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(ConsumptionMode.OPTIMAL);
    lenient()
        .when(survivalPredictor.checkIfEvacuationIsNeeded(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(false);
    lenient().when(powerGridSimulator.process(any(Float.class), any())).thenReturn(false);
  }

  @ParameterizedTest(name = "Stock: {0} O2, Consumption: {1}/day -> Expected result: {2}")
  @CsvSource({
    "100.0,  5.0, SUCCESS", // Enough for 20 days. Mission lasts 7. Safe.
    "35.0,   5.0, SUCCESS", // Just enough (7 * 5 = 35).
    "15.0,   5.0, FAILURE", // Stock for 3 days. Simulator will generate deficit. Death.
    "0.0,   10.0, FAILURE" // Empty starting inventory.
  })
  @DisplayName("Survival simulation (Simulator + Evaluator orchestration)")
  void shouldSimulateSurvivalBasedOnOxygen(
      float startingOxygen, float dailyConsumption, Status expectedStatus) {

    // given
    List<Resource> startingResources = List.of(new Resource(ResourceType.OXYGEN, startingOxygen));

    // Force specific consumption on the calculator
    lenient()
        .when(demandCalculator.calculateCrewDemand(any(), any()))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, dailyConsumption)));

    // when: Generate raw timeline
    List<DailyState> timeline =
        timelineSimulator.simulate(mockManifest, new ArrayList<>(), startingResources);

    // then: Use the real evaluator to check if simulator math worked
    SimulationOutcomeEvaluator evaluator = new SimulationOutcomeEvaluator();
    SimulationOutcome outcome = evaluator.evaluate(timeline, mockManifest);

    DailyState lastState = timeline.getLast();
    float finalOxygen =
        lastState.getWarehouse().stream()
            .filter(r -> r.getType() == ResourceType.OXYGEN)
            .map(Resource::getAmount)
            .findFirst()
            .orElse(-999f);

    String errorMsg =
        String.format(
            "❌ Expected: %s, Returned: %s. Stopped at sol: %d. O2 level: %.2f",
            expectedStatus, outcome.getStatus(), lastState.getSol(), finalOxygen);

    assertEquals(expectedStatus, outcome.getStatus(), errorMsg);
  }

  @Test
  @DisplayName("Successful rescue just in time (EVACUATION) - Stock depletes after rocket arrival")
  void shouldResultInEvacuationWhenRescueArrivesJustInTime() {
    // --- GIVEN ---
    // Oxygen stock: 3.5. Consumption: 1.0/day.
    // Sol 1: 2.5
    // Sol 2: 1.5
    // Sol 3: 0.5
    // Sol 4: -0.5 (DEATH!)
    List<Resource> startingResources = List.of(new Resource(ResourceType.OXYGEN, 3.5f));

    lenient()
        .when(demandCalculator.calculateCrewDemand(any(), any()))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 1.0f)));

    // Predictor raises alarm immediately in Sol 1 (Rescue rocket flight days = 2)
    // Rocket will arrive in Sol: 1 + 2 = 3.
    when(survivalPredictor.checkIfEvacuationIsNeeded(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(true);

    // --- WHEN ---
    List<DailyState> timeline =
        timelineSimulator.simulate(mockManifest, new ArrayList<>(), startingResources);

    SimulationOutcomeEvaluator evaluator = new SimulationOutcomeEvaluator();
    SimulationOutcome outcome = evaluator.evaluate(timeline, mockManifest);

    // --- THEN ---
    // Death would theoretically occur in Sol 4, but rescue arrived in Sol 3. Mission ends with
    // EVACUATION status!
    assertEquals(
        Status.EVACUATION,
        outcome.getStatus(),
        "The rocket should save the crew from death by lack of oxygen!");
    assertEquals(4, outcome.getDeathSol(), "Death would theoretically occur in Sol 4");
    assertEquals(3, outcome.getEvacuationSol(), "Evacuation should take place in Sol 3");
  }

  @Test
  @DisplayName("Correctly tag DELIVERY_RECEIVED only on delivery day")
  void shouldTagDeliveryReceivedExactlyOnDeliveryDay() {
    // --- GIVEN ---
    // Manifest with delivery set on Sol 3
    Delivery delivery =
        new Delivery(3, List.of(new Resource(ResourceType.FOOD, 100f)), new ArrayList<>());
    mockManifest = mockManifest.copyWithDeliveries(List.of(delivery));

    // --- WHEN ---
    List<DailyState> timeline =
        timelineSimulator.simulate(mockManifest, new ArrayList<>(), new ArrayList<>());

    // --- THEN ---
    DailyState sol1 = timeline.get(0); // Index 0 is Sol 1
    DailyState sol2 = timeline.get(1);
    DailyState sol3 = timeline.get(2); // Index 2 is Sol 3

    assertTrue(
        sol1.getObservations().stream().noneMatch(o -> o == ObservationType.DELIVERY_RECEIVED),
        "There should be no delivery on Sol 1");
    assertTrue(
        sol2.getObservations().stream().noneMatch(o -> o == ObservationType.DELIVERY_RECEIVED),
        "There should be no delivery on Sol 2");
    assertTrue(
        sol3.getObservations().contains(ObservationType.DELIVERY_RECEIVED),
        "The delivery tag MUST be present in the history for Sol 3!");
  }

  @Test
  @DisplayName("Return to OPTIMAL mode and apply correct tag after crisis ends")
  void shouldTagOptimalModeWhenRecoveringFromMinimal() {
    // --- GIVEN ---
    // Mock survival predictor behavior to simulate a fluctuating crisis
    // Sol 1: Conserving (MINIMAL)
    // Sol 2: Still conserving (MINIMAL)
    // Sol 3: Delivery saved the situation, returning to normal (OPTIMAL)
    when(survivalPredictor.evaluateCrewConsumptionMode(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(ConsumptionMode.MINIMAL) // Sol 1
        .thenReturn(ConsumptionMode.MINIMAL) // Sol 2
        .thenReturn(ConsumptionMode.OPTIMAL); // Sol 3 and onwards

    // --- WHEN ---
    List<DailyState> timeline =
        timelineSimulator.simulate(mockManifest, new ArrayList<>(), new ArrayList<>());

    // --- THEN ---
    DailyState sol1 = timeline.get(0);
    DailyState sol2 = timeline.get(1);
    DailyState sol3 = timeline.get(2);

    // Change OPTIMAL (starting) -> MINIMAL
    assertTrue(
        sol1.getObservations().contains(ObservationType.MINIMAL_DEMAND_ACTIVATED),
        "There should be a MINIMAL transition tag on Sol 1");

    // No change (MINIMAL -> MINIMAL) - no tag spam!
    assertTrue(
        sol2.getObservations().isEmpty(),
        "Mode did not change on Sol 2, Event Log should be clean (no spam)");

    // Change MINIMAL -> OPTIMAL
    assertTrue(
        sol3.getObservations().contains(ObservationType.OPTIMAL_DEMAND_ACTIVATED),
        "There should be a return to OPTIMAL tag on Sol 3");
  }

  @Test
  @DisplayName("Correctly tag TOTAL_BLACKOUT upon power failure")
  void shouldTagTotalBlackoutWhenEnergyProcessorFails() {
    // --- GIVEN ---
    // Energy processor reports power is out!
    when(powerGridSimulator.process(any(Float.class), any())).thenReturn(true);

    // --- WHEN ---
    List<DailyState> timeline =
        timelineSimulator.simulate(mockManifest, new ArrayList<>(), new ArrayList<>());

    // --- THEN ---
    // Check if simulator correctly added the note to the Event Log
    assertTrue(
        timeline.getFirst().getObservations().contains(ObservationType.TOTAL_BLACKOUT),
        "Expected TOTAL_BLACKOUT tag on the first day of simulation");
  }

  @Test
  @DisplayName("Correctly apply tags upon activation of MINIMAL and SOS modes")
  void shouldTagEvacuationAndMinimalModeWhenPredictorTriggers() {
    // --- GIVEN ---
    when(survivalPredictor.checkIfEvacuationIsNeeded(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(true);
    when(survivalPredictor.evaluateCrewConsumptionMode(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(ConsumptionMode.MINIMAL);

    // --- WHEN ---
    List<DailyState> timeline =
        timelineSimulator.simulate(mockManifest, new ArrayList<>(), new ArrayList<>());

    // --- THEN ---
    DailyState dayOne = timeline.getFirst();
    assertTrue(
        dayOne.getObservations().contains(ObservationType.EVACUATION_ALERT),
        "Expected EVACUATION_ALERT tag");
    assertTrue(
        dayOne.getObservations().contains(ObservationType.MINIMAL_DEMAND_ACTIVATED),
        "Expected MINIMAL_DEMAND_ACTIVATED tag due to transition from OPTIMAL");
  }
}
