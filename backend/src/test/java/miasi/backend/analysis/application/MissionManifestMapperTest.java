package miasi.backend.analysis.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import miasi.backend.analysis.application.service.MissionManifestMapper;
import miasi.backend.analysis.domain.model.input.MissionManifest;
import miasi.backend.analysis.domain.model.schedule.ImpactTarget;
import miasi.backend.analysis.domain.model.schedule.ImpactType;
import miasi.backend.common.domain.model.ModuleState;
import miasi.backend.common.domain.model.ResourceType;
import miasi.backend.configuration.domain.model.MissionPlan;
import miasi.backend.configuration.domain.model.Module;
import miasi.backend.configuration.domain.model.ModuleCatalog;
import miasi.backend.configuration.domain.model.ModuleType;
import miasi.backend.configuration.domain.model.Resources;
import miasi.backend.configuration.domain.model.SexProfile;
import miasi.backend.schedule.domain.model.DeliveryContent;
import miasi.backend.schedule.domain.model.DeliveryItem;
import miasi.backend.schedule.domain.model.DeliveryItemType;
import miasi.backend.schedule.domain.model.EventType;
import miasi.backend.schedule.domain.model.MissionSchedule;
import miasi.backend.schedule.domain.model.ScheduleStatus;
import miasi.backend.schedule.domain.model.ScheduledEvent;
import miasi.backend.schedule.domain.model.SupplyDelivery;
import miasi.backend.schedule.domain.model.Threat;
import miasi.backend.schedule.domain.model.ThreatType;
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
