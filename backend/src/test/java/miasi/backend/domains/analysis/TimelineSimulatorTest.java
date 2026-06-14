package miasi.backend.domains.analysis;

import miasi.backend.domains.analisis.services.*;
import miasi.backend.domains.analisis.simulation.SimulationVariant;
import miasi.backend.domains.analisis.simulation.Status;
import miasi.backend.domains.analisis.simulation.VariantType;
import miasi.backend.domains.analisis.types.core.DailyState;
import miasi.backend.domains.analisis.types.core.Resource;
import miasi.backend.domains.analisis.types.crew.ConsumptionMode;
import miasi.backend.domains.analisis.types.input.MissionManifest;
import miasi.backend.domains.analisis.types.modules.Module;
import miasi.backend.domains.analisis.types.schedule.Delivery;
import miasi.backend.enums.ModuleState;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class TimelineSimulatorTest {

    @Mock
    private DemandCalculator demandCalculator;
    @Mock
    private ProductionCalculator productionCalculator;


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
    }

    @ParameterizedTest(name = "Zapas: {0} O2, Zużycie: {1}/dzień -> Oczekiwany wynik: {2}")
    @CsvSource({
            "100.0,  5.0, SUCCESS",  // Wystarczy na 20 dni. Misja trwa 7. Spokój.
            "35.0,   5.0, SUCCESS",  // Idealnie na styk (7 * 5 = 35).
            "15.0,   5.0, FAILURE",  // Zapasu na 3 dni. Racjonowanie przedłuży życie do ok. 5 dni, ale misja trwa 7. Śmierć.
            "0.0,   10.0, FAILURE"   // Pusto w magazynie startowym.
    })
    @DisplayName("Symulacja przetrwania załogi na podstawie początkowego zapasu")
    void shouldSimulateSurvivalBasedOnOxygen(float startingOxygen, float dailyConsumption, Status expectedStatus) {

        List<Resource> startingResources = List.of(new Resource(ResourceType.OXYGEN, startingOxygen));

        lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.OPTIMAL)))
                .thenReturn(List.of(new Resource(ResourceType.OXYGEN, dailyConsumption)));
        lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.MINIMAL)))
                .thenReturn(List.of(new Resource(ResourceType.OXYGEN, dailyConsumption / 2f)));

        SimulationVariant result = timelineSimulator.simulate(
                mockManifest, new ArrayList<>(), startingResources, VariantType.IDEAL
        );

        DailyState lastState = result.getTimeline().get(result.getTimeline().size() - 1);
        float finalOxygen = lastState.getWarehouse().stream()
                .filter(r -> r.getType() == ResourceType.OXYGEN).map(Resource::getAmount).findFirst().orElse(-999f);

        String errorMsg = String.format("❌ Oczekiwano: %s, Zwrócono: %s. Zatrzymano na sol: %d. Stan O2: %.2f",
                expectedStatus, result.getStatus(), lastState.getSol(), finalOxygen);

        assertEquals(expectedStatus, result.getStatus(), errorMsg);
    }

    @Test
    @DisplayName("Zignorowanie paniki i pozostanie w trybie OPTIMAL, gdy dostawa jest blisko")
    void shouldStayOptimalWhenDeliveryIsNear() {
        // --- GIVEN ---
        // Zaczynamy z 10 tlenu. Zużycie to 5. Zapasu starczy zaledwie na 2 dni!
        List<Resource> startingResources = List.of(new Resource(ResourceType.OXYGEN, 10.0f));

        lenient().when(demandCalculator.calculateCrewDemand(any(), eq(ConsumptionMode.OPTIMAL)))
                .thenReturn(List.of(new Resource(ResourceType.OXYGEN, 5.0f)));

        // Ale uwaga! W dniu 2. przylatuje potężna dostawa tlenu z Ziemi.
        Delivery rescueDelivery = new Delivery(2, List.of(new Resource(ResourceType.OXYGEN, 100.0f)), new ArrayList<>());
        mockManifest.setDeliveries(List.of(rescueDelivery));

        // --- WHEN ---
        SimulationVariant result = timelineSimulator.simulate(
                mockManifest, new ArrayList<>(), startingResources, VariantType.IDEAL
        );

        // --- THEN ---
        List<DailyState> timeline = result.getTimeline();

        // Ponieważ w Sol 1 system widzi, że tlenu starczy na 2 dni (10/5), a dostawa jest JUTRO (za 1 dzień),
        // to dni_do_pusto (2) > dni_do_dostawy (1). Załoga nie musi panikować!
        assertEquals(ConsumptionMode.OPTIMAL, timeline.getFirst().getMode(),
                "W dniu 1 załoga nie powinna oszczędzać, bo wie o jutrzejszej dostawie!");
    }

    @Test
    @DisplayName("Całkowity blackout przy niewystarczającej mocy")
    void shouldTriggerTotalBlackoutWhenPowerIsInsufficient() {
        // --- GIVEN ---
        List<Resource> startingResources = List.of(new Resource(ResourceType.ENERGY, 5.0f)); // Bateria ma 5 prądu

        // Tworzymy działający sprzęt
        Module farm = new Module("Farm", 1000f, 1, 1, new ArrayList<>(), new ArrayList<>(), ModuleState.ACTIVE, 1.0f);
        Module waterFilter = new Module("Filter", 200f, 1, 1, new ArrayList<>(), new ArrayList<>(), ModuleState.ACTIVE, 1.0f);
        List<Module> activeModules = List.of(farm, waterFilter);

        // Baza nic nie produkuje, a moduły krzyczą, że chcą 15 prądu! (Bateria ma tylko 5)
        lenient().when(productionCalculator.calculateModulesProduction(any()))
                .thenReturn(new ArrayList<>());
        lenient().when(demandCalculator.calculateModulesDemand(any()))
                .thenReturn(List.of(new Resource(ResourceType.ENERGY, 15.0f)));

        // --- WHEN ---
        SimulationVariant result = timelineSimulator.simulate(
                mockManifest, activeModules, startingResources, VariantType.IDEAL
        );

        // --- THEN ---
        List<Module> stateAfterBlackout = result.getTimeline().getFirst().getModules();

        assertEquals(ModuleState.INACTIVE, stateAfterBlackout.get(0).getStatus(), "Farma powinna zostać odcięta (Blackout)");
        assertEquals(ModuleState.INACTIVE, stateAfterBlackout.get(1).getStatus(), "Filtr powinien zostać odcięty (Blackout)");
    }
}