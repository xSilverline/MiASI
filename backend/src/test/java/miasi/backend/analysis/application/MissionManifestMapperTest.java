package miasi.backend.analysis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import miasi.backend.domains.analysis.types.input.MissionManifest;
import miasi.backend.domains.analysis.types.schedule.ImpactTarget;
import miasi.backend.domains.analysis.types.schedule.ImpactType;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import miasi.backend.domains.configuration.modules.ModuleType;
import miasi.backend.domains.configuration.other.Resources;
import miasi.backend.domains.configuration.other.SexProfile;
import miasi.backend.domains.schedule.DeliveryContent;
import miasi.backend.domains.schedule.DeliveryItem;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.SupplyDelivery;
import miasi.backend.domains.schedule.Threat;
import miasi.backend.schedule.domain.DeliveryItemType;
import miasi.backend.schedule.domain.EventType;
import miasi.backend.schedule.domain.ScheduleStatus;
import miasi.backend.schedule.domain.ThreatType;
import miasi.backend.sharedkernel.model.ModuleState;
import miasi.backend.sharedkernel.model.ResourceType;
import org.junit.jupiter.api.Test;

class MissionManifestMapperTest {

  @Test
  void toManifest_shouldMapConfigurationAndScheduleIntoAnalysisInput() {
    // given
    ModuleType habitatType =
        new ModuleType(
            "habitat",
            List.of(new Resources(ResourceType.ENERGY, 2f)),
            List.of(new Resources(ResourceType.OXYGEN, 4f)));
    Module habitat = new Module("habitat-1", ModuleState.ACTIVE, habitatType, 100f);
    MissionPlan missionPlan =
        new MissionPlan(
            List.of(
                new SexProfile(
                    "crew", 2, Map.of(ResourceType.OXYGEN, 1f), Map.of(ResourceType.OXYGEN, 2f))),
            30,
            List.of(),
            List.of(habitat),
            500f);
    MissionSchedule schedule =
        new MissionSchedule(
            "schedule-1",
            "plan-1",
            40,
            ScheduleStatus.READY_FOR_ANALYSIS,
            List.of(delivery(), threat()));

    // when
    MissionManifest manifest =
        new MissionManifestMapper()
            .toManifest(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                missionPlan,
                new ModuleCatalog(List.of(habitat), List.of(habitatType)),
                schedule);

    // then
    assertEquals(40, manifest.getDurationSols());
    assertEquals(500f, manifest.getMaxWeightSolZero());
    assertEquals(1, manifest.getCrew().size());
    assertEquals(2, manifest.getCrew().getFirst().getCount());
    assertEquals(1, manifest.getCatalog().getFirst().getMinCount());
    assertEquals(
        ResourceType.OXYGEN, manifest.getCatalog().getFirst().getProduction().getFirst().getType());
    assertEquals(1, manifest.getDeliveries().size());
    assertEquals(
        ResourceType.WATER,
        manifest.getDeliveries().getFirst().getResources().getFirst().getType());
    assertFalse(manifest.getDeliveries().getFirst().getModules().isEmpty());
    assertEquals(1, manifest.getThreats().size());
    assertEquals(ImpactType.STATE_CHANGE, manifest.getThreats().getFirst().getType());
    assertEquals(ImpactTarget.MODULE, manifest.getThreats().getFirst().getTarget());
  }

  private SupplyDelivery delivery() {
    SupplyDelivery delivery =
        new SupplyDelivery(
            new DeliveryContent(
                List.of(
                    new DeliveryItem("WATER", DeliveryItemType.RESOURCE, 12, 1),
                    new DeliveryItem("habitat-1", DeliveryItemType.MODULE, 1, 100)),
                112));
    delivery.setId("delivery-1");
    delivery.setType(EventType.SUPPLY_DELIVERY);
    delivery.setSol(5);
    delivery.setDescription("Water and habitat delivery");
    return delivery;
  }

  private ScheduledEvent threat() {
    Threat threat = new Threat(ThreatType.MODULE_FAILURE, "habitat-1", 1.0, 2, "state");
    threat.setId("threat-1");
    threat.setType(EventType.THREAT);
    threat.setSol(10);
    threat.setDescription("Habitat failure");
    return threat;
  }
}
