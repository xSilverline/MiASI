package miasi.backend.domains.analysis;

import miasi.backend.domains.analisis.services.DemandCalculator;
import miasi.backend.domains.analisis.services.ProductionCalculator;
import miasi.backend.domains.analisis.services.SurvivalPredictor;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.schedule.Delivery;
import miasi.backend.enums.ResourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class SurvivalPredictorTest {

  @Mock
  private DemandCalculator demandCalculator;
  @Mock
  private ProductionCalculator productionCalculator;

  @InjectMocks
  private SurvivalPredictor survivalPredictor;

  private MissionManifest mockManifest;

  @BeforeEach
  void setUp() {
    // Standardowa misja trwająca 10 dni (na potrzeby horyzontu planowania)
    mockManifest = new MissionManifest(
        null, 10, 0, 20000f,
        new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()
    );

    // Baza domyślnie nic nie produkuje
    lenient().when(productionCalculator.calculateModulesProduction(any()))
        .thenReturn(new ArrayList<>());
  }

  @Test
  @DisplayName("Powinien zachować tryb OPTIMAL, gdy zapasów tlenu jest pod dostatkiem")
  void shouldStayOptimalWhenResourcesAreAbundant() {
    // --- GIVEN ---
    // Mamy 100 tlenu, zużycie to 5/dzień. Spokojnie starczy na cały horyzont (10 dni)
    List<Resource> warehouse = List.of(new Resource(ResourceType.OXYGEN, 100.0f));

    lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.OPTIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 5.0f)));

    // --- WHEN ---
    ConsumptionMode mode = survivalPredictor.evaluateCrewConsumptionMode(
        1, 10, warehouse, new ArrayList<>(), mockManifest
    );

    // --- THEN ---
    assertEquals(ConsumptionMode.OPTIMAL, mode, "Załoga powinna jeść/oddychać normalnie");
  }

  @Test
  @DisplayName("Powinien przełączyć na MINIMAL, gdy zapasy wyczerpią się przed końcem misji")
  void shouldSwitchToMinimalWhenResourcesAreRunningLow() {
    // --- GIVEN ---
    // Zaczynamy z 10 tlenu. Przy zużyciu OPTIMAL=5, zapas skończy się za 2 dni, a misja trwa jeszcze 10 dni.
    List<Resource> warehouse = List.of(new Resource(ResourceType.OXYGEN, 10.0f));

    lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.OPTIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 5.0f)));

    // --- WHEN ---
    ConsumptionMode mode = survivalPredictor.evaluateCrewConsumptionMode(
        1, 10, warehouse, new ArrayList<>(), mockManifest
    );

    // --- THEN ---
    assertEquals(ConsumptionMode.MINIMAL, mode, "System powinien wymusić tryb oszczędny");
  }

  @Test
  @DisplayName("Zignorowanie paniki i pozostanie w trybie OPTIMAL, gdy dostawa ratuje sytuację")
  void shouldStayOptimalWhenDeliveryIsNear() {
    // --- GIVEN ---
    // Zaczynamy z 10 tlenu. Zużycie OPTIMAL to 5. Zapasu fizycznie starczy na 2 dni.
    List<Resource> warehouse = List.of(new Resource(ResourceType.OXYGEN, 10.0f));

    lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.OPTIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 5.0f)));

    // Ale w Sol 2 (czyli jutro) przylatuje zrzut zapasów
    Delivery delivery = new Delivery(2, List.of(new Resource(ResourceType.OXYGEN, 50.0f)), new ArrayList<>());
    mockManifest.setDeliveries(List.of(delivery));

    // --- WHEN ---
    // Jesteśmy w Sol 1. Do celu (dostawy) mamy 2 - 1 + 1 = 2 dni. Zapasy starczą na 10/5 = 2 dni.
    ConsumptionMode mode = survivalPredictor.evaluateCrewConsumptionMode(
        1, 10, warehouse, new ArrayList<>(), mockManifest
    );

    // --- THEN ---
    assertEquals(ConsumptionMode.OPTIMAL, mode,
        "Załoga nie powinna przechodzić na MINIMAL, bo jutrzejsza dostawa zabezpiecza zapotrzebowanie");
  }

  @Test
  @DisplayName("Powinien podnieść alarm ewakuacyjny (EVACUATION_ALERT), gdy nawet tryb MINIMAL nie uratuje załogi")
  void shouldTriggerEvacuationWhenEvenMinimalIsInsufficient() {
    // --- GIVEN ---
    // Skrajny kryzys: mamy tylko 2 tlenu.
    List<Resource> warehouse = List.of(new Resource(ResourceType.OXYGEN, 2.0f));

    // Nawet jeśli zacisną pasy (MINIMAL), zużywają 3/dzień. Umrą przed końcem następnego dnia.
    lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.MINIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 3.0f)));

    // --- WHEN ---
    boolean evacuationNeeded = survivalPredictor.checkIfEvacuationIsNeeded(
        1, 10, warehouse, new ArrayList<>(), mockManifest
    );

    // --- THEN ---
    assertTrue(evacuationNeeded, "Sytuacja jest beznadziejna, system powinien natychmiast zażądać ewakuacji");
  }

  @Test
  @DisplayName("Nie powinien podnosić alarmu ewakuacyjnego, jeśli tryb MINIMAL pozwala dociągnąć do dostawy")
  void shouldNotTriggerEvacuationWhenMinimalSavesTheDay() {
    // --- GIVEN ---
    // Mamy 6 tlenu. Do kolejnej dostawy zostało 3 dni.
    List<Resource> warehouse = List.of(new Resource(ResourceType.OXYGEN, 6.0f));

    // W trybie MINIMAL zużywają 2/dzień. 6 / 2 = 3 dni przetrwania. Idealnie na styk!
    lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.MINIMAL)))
        .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 2.0f)));

    // Dostawa jest w Sol 4 (za 3 dni od Sol 1)
    Delivery delivery = new Delivery(4, List.of(new Resource(ResourceType.OXYGEN, 50.0f)), new ArrayList<>());
    mockManifest.setDeliveries(List.of(delivery));

    // --- WHEN ---
    boolean evacuationNeeded = survivalPredictor.checkIfEvacuationIsNeeded(
        1, 10, warehouse, new ArrayList<>(), mockManifest
    );

    // --- THEN ---
    assertFalse(evacuationNeeded, "Alarm ewakuacyjny nie powinien się włączyć, bo tryb MINIMAL daje szansę na doczekanie dostawy");
  }
}