package miasi.backend.domains.analysis;

import miasi.backend.domains.analisis.services.*;
import miasi.backend.domains.analisis.simulation.Status;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.domains.analisis.types.core.ObservationType;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.result.SimulationOutcome;
import miasi.backend.domains.analisis.types.schedule.Delivery;
import miasi.backend.enums.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimelineSimulatorTest {

  // Mockujemy wszystkie procesory i kalkulatory
  @Mock
  private DemandCalculator demandCalculator;
  @Mock
  private ProductionCalculator productionCalculator;
  @Mock
  private DeliveryProcessor deliveryProcessor;
  @Mock
  private ThreatProcessor threatProcessor;
  @Mock
  private EnergyProcessor energyProcessor;
  @Mock
  private SurvivalPredictor survivalPredictor;

  @InjectMocks
  private TimelineSimulator timelineSimulator;

  private MissionManifest mockManifest;

  @BeforeEach
  void setUp() {
    // Misja: 5 dni głównej + 2 dni ratunku = 7 dni celu
    mockManifest = new MissionManifest(
        null, 5, 2, 20000f,
        new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
    );

    lenient().when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(new ArrayList<>());
    lenient().when(demandCalculator.calculateModulesDemand(any()))
        .thenReturn(new ArrayList<>());
    lenient().when(demandCalculator.calculateCrewDemand(any(), any()))
        .thenReturn(new ArrayList<>());

    lenient().when(survivalPredictor.evaluateCrewConsumptionMode(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(ConsumptionMode.OPTIMAL);
    lenient().when(survivalPredictor.checkIfEvacuationIsNeeded(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(false);
    lenient().when(energyProcessor.process(any(Float.class), any()))
        .thenReturn(false);
  }

  @ParameterizedTest(name = "Zapas: {0} O2, Zużycie: {1}/dzień -> Oczekiwany wynik: {2}")
  @CsvSource({
      "100.0,  5.0, SUCCESS",  // Wystarczy na 20 dni. Misja trwa 7. Spokój.
      "35.0,   5.0, SUCCESS",  // Idealnie na styk (7 * 5 = 35).
      "15.0,   5.0, FAILURE",  // Zapasu na 3 dni. Symulator wygeneruje deficyt. Śmierć.
      "0.0,   10.0, FAILURE"   // Pusto w magazynie startowym.
  })
  @DisplayName("Symulacja przetrwania (Orkiestracja Symulatora + Ewaluatora)")
  void shouldSimulateSurvivalBasedOnOxygen(float startingOxygen, float dailyConsumption, Status expectedStatus) {

    // given
    List<Resource> startingResources = List.of(new Resource(ResourceType.OXYGEN, startingOxygen));

    // Wymuszamy na kalkulatorze określone zużycie
    lenient().when(demandCalculator.calculateCrewDemand(any(), any()))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, dailyConsumption)));

    // when: Generujemy surową oś czasu
    List<DailyState> timeline = timelineSimulator.simulate(
        mockManifest, new ArrayList<>(), startingResources
    );

    // then: Używamy prawdziwego ewaluatora, aby sprawdzić czy matematyka symulatora zadziałała
    SimulationOutcomeEvaluator evaluator = new SimulationOutcomeEvaluator();
    SimulationOutcome outcome = evaluator.evaluate(timeline, mockManifest);

    DailyState lastState = timeline.getLast();
    float finalOxygen = lastState.getWarehouse().stream()
        .filter(r -> r.getType() == ResourceType.OXYGEN).map(Resource::getAmount).findFirst().orElse(-999f);

    String errorMsg = String.format("❌ Oczekiwano: %s, Zwrócono: %s. Zatrzymano na sol: %d. Stan O2: %.2f",
        expectedStatus, outcome.getStatus(), lastState.getSol(), finalOxygen);

    assertEquals(expectedStatus, outcome.getStatus(), errorMsg);
  }

  @Test
  @DisplayName("Udany ratunek na styk (EVACUATION) - Zapas kończy się po przylocie rakiety")
  void shouldResultInEvacuationWhenRescueArrivesJustInTime() {
    // --- GIVEN ---
    // Zapas tlenu: 3.5. Zużycie: 1.0/dzień.
    // Sol 1: 2.5
    // Sol 2: 1.5
    // Sol 3: 0.5
    // Sol 4: -0.5 (ŚMIERĆ!)
    List<Resource> startingResources = List.of(new Resource(ResourceType.OXYGEN, 3.5f));

    lenient().when(demandCalculator.calculateCrewDemand(any(), any()))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 1.0f)));

    // Predictor bije na alarm od razu w Sol 1 (Dni lotu rakiety ratunkowej = 2)
    // Rakieta przyleci w Sol: 1 + 2 = 3.
    when(survivalPredictor.checkIfEvacuationIsNeeded(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(true);

    // --- WHEN ---
    List<DailyState> timeline = timelineSimulator.simulate(
        mockManifest, new ArrayList<>(), startingResources
    );

    SimulationOutcomeEvaluator evaluator = new SimulationOutcomeEvaluator();
    SimulationOutcome outcome = evaluator.evaluate(timeline, mockManifest);

    // --- THEN ---
    // Zgon nastąpiłby w Sol 4, ale ratunek przyszedł w Sol 3. Misja kończy się statusem EVACUATION!
    assertEquals(Status.EVACUATION, outcome.getStatus(),
        "Rakieta powinna zdążyć uratować załogę przed śmiercią z braku tlenu!");
    assertEquals(4, outcome.getDeathSol(), "Śmierć teoretycznie nastąpiłaby w Sol 4");
    assertEquals(3, outcome.getEvacuationSol(), "Ewakuacja powinna mieć miejsce w Sol 3");
  }

  @Test
  @DisplayName("Prawidłowe przypinanie tagu DELIVERY_RECEIVED tylko w dniu dostawy")
  void shouldTagDeliveryReceivedExactlyOnDeliveryDay() {
    // --- GIVEN ---
    // Manifest z dostawą ustaloną na Sol 3
    Delivery delivery = new Delivery(3, List.of(new Resource(ResourceType.FOOD, 100f)), new ArrayList<>());
    mockManifest.setDeliveries(List.of(delivery));

    // --- WHEN ---
    List<DailyState> timeline = timelineSimulator.simulate(
        mockManifest, new ArrayList<>(), new ArrayList<>()
    );

    // --- THEN ---
    DailyState sol1 = timeline.get(0); // Index 0 to Sol 1
    DailyState sol2 = timeline.get(1);
    DailyState sol3 = timeline.get(2); // Index 2 to Sol 3

    assertTrue(sol1.getObservations().stream().noneMatch(o -> o == ObservationType.DELIVERY_RECEIVED),
        "W Sol 1 nie powinno być dostawy");
    assertTrue(sol2.getObservations().stream().noneMatch(o -> o == ObservationType.DELIVERY_RECEIVED),
        "W Sol 2 nie powinno być dostawy");
    assertTrue(sol3.getObservations().contains(ObservationType.DELIVERY_RECEIVED),
        "Tag dostawy MUSI znajdować się w historii dla Sol 3!");
  }

  @Test
  @DisplayName("Powrót do trybu OPTIMAL i nałożenie odpowiedniego tagu po zakończeniu kryzysu")
  void shouldTagOptimalModeWhenRecoveringFromMinimal() {
    // --- GIVEN ---
    // Mockujemy zachowanie wróżki przetrwania (Predictora), by symulować falujący kryzys
    // Sol 1: Oszczędzamy (MINIMAL)
    // Sol 2: Nadal oszczędzamy (MINIMAL)
    // Sol 3: Dostawa uratowała sytuację, wracamy do normy (OPTIMAL)
    when(survivalPredictor.evaluateCrewConsumptionMode(anyInt(), anyInt(), any(), any(), any()))
        .thenReturn(ConsumptionMode.MINIMAL)  // Sol 1
        .thenReturn(ConsumptionMode.MINIMAL)  // Sol 2
        .thenReturn(ConsumptionMode.OPTIMAL); // Sol 3 i dalej

    // --- WHEN ---
    List<DailyState> timeline = timelineSimulator.simulate(
        mockManifest, new ArrayList<>(), new ArrayList<>()
    );

    // --- THEN ---
    DailyState sol1 = timeline.get(0);
    DailyState sol2 = timeline.get(1);
    DailyState sol3 = timeline.get(2);

    // Zmiana OPTIMAL (startowy) -> MINIMAL
    assertTrue(sol1.getObservations().contains(ObservationType.MINIMAL_DEMAND_ACTIVATED),
        "W Sol 1 powinien być tag przejścia na MINIMAL");

    // Brak zmiany (MINIMAL -> MINIMAL) - nie spamujemy tagami!
    assertTrue(sol2.getObservations().isEmpty(),
        "W Sol 2 tryb się nie zmienił, Dziennik Zdarzeń powinien być czysty (brak spamu)");

    // Zmiana MINIMAL -> OPTIMAL
    assertTrue(sol3.getObservations().contains(ObservationType.OPTIMAL_DEMAND_ACTIVATED),
        "W Sol 3 powinien być tag powrotu do trybu OPTIMAL");
  }

  @Test
  @DisplayName("Prawidłowe nakładanie tagu TOTAL_BLACKOUT przy awarii zasilania")
  void shouldTagTotalBlackoutWhenEnergyProcessorFails() {
    // --- GIVEN ---
    // Procesor energii zgłasza, że prądu brakło!
    when(energyProcessor.process(any(Float.class), any())).thenReturn(true);

    // --- WHEN ---
    List<DailyState> timeline = timelineSimulator.simulate(
        mockManifest, new ArrayList<>(), new ArrayList<>()
    );

    // --- THEN ---
    // Sprawdzamy czy symulator poprawnie "nakleił" karteczkę w Dzienniku Zdarzeń
    assertTrue(timeline.getFirst().getObservations().contains(ObservationType.TOTAL_BLACKOUT),
        "Oczekiwano tagu TOTAL_BLACKOUT w pierwszym dniu symulacji");
  }

  @Test
  @DisplayName("Prawidłowe nakładanie tagów przy aktywacji trybu MINIMAL i SOS")
  void shouldTagEvacuationAndMinimalModeWhenPredictorTriggers() {
    // --- GIVEN ---
    when(survivalPredictor.checkIfEvacuationIsNeeded(anyInt(), anyInt(), any(), any(), any())).thenReturn(true);
    when(survivalPredictor.evaluateCrewConsumptionMode(anyInt(), anyInt(), any(), any(), any())).thenReturn(ConsumptionMode.MINIMAL);

    // --- WHEN ---
    List<DailyState> timeline = timelineSimulator.simulate(
        mockManifest, new ArrayList<>(), new ArrayList<>()
    );

    // --- THEN ---
    DailyState dayOne = timeline.getFirst();
    assertTrue(dayOne.getObservations().contains(ObservationType.EVACUATION_ALERT),
        "Oczekiwano tagu EVACUATION_ALERT");
    assertTrue(dayOne.getObservations().contains(ObservationType.MINIMAL_DEMAND_ACTIVATED),
        "Oczekiwano tagu MINIMAL_DEMAND_ACTIVATED ze względu na zmianę z OPTIMAL");
  }
}