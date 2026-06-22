package miasi.backend.common.infrastructure.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import miasi.backend.analysis.domain.model.AnalysisScheduleEventInbox;
import miasi.backend.analysis.domain.model.baseline.BaselineAnalysisCompletedEvent;
import miasi.backend.analysis.infrastructure.in.event.AnalysisScheduleEventListener;
import miasi.backend.common.domain.model.event.EventProcessingStatus;
import miasi.backend.common.domain.model.event.MissionPlanCreated;
import miasi.backend.common.domain.model.event.MissionScheduleCreated;
import miasi.backend.schedule.application.service.ScheduleApplicationService;
import miasi.backend.schedule.domain.model.EventType;
import miasi.backend.schedule.domain.model.MissionPlanEventInbox;
import miasi.backend.schedule.domain.model.MissionSchedule;
import miasi.backend.schedule.domain.model.Threat;
import miasi.backend.schedule.domain.model.ThreatType;
import miasi.backend.visualization.domain.model.VisualizationAnalysisEventInbox;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EdaRoutingTest {

  @Autowired private ApplicationEventPublisher eventPublisher;

  @Autowired private MissionPlanEventInbox missionPlanInbox;

  @Autowired private AnalysisScheduleEventInbox analysisScheduleInbox;

  @Autowired private VisualizationAnalysisEventInbox visualizationAnalysisInbox;

  @Autowired private ScheduleApplicationService scheduleApplicationService;

  @BeforeEach
  void clearInboxes() {
    missionPlanInbox.clear();
    analysisScheduleInbox.clear();
    visualizationAnalysisInbox.clear();
  }

  @Test
  void shouldRouteEventsAcrossInProcessSpringEventBus() {
    eventPublisher.publishEvent(MissionPlanCreated.create(1));
    eventPublisher.publishEvent(MissionScheduleCreated.create("schedule-1", "1"));
    eventPublisher.publishEvent(
        BaselineAnalysisCompletedEvent.create(UUID.randomUUID(), List.of(), null));

    assertEquals(1, missionPlanInbox.getMissionPlanCreatedEntries().size());
    assertEquals(
        EventProcessingStatus.PROCESSED,
        missionPlanInbox.getMissionPlanCreatedEntries().getFirst().status());
    assertEquals(1, analysisScheduleInbox.getEntries().size());
    assertEquals(
        EventProcessingStatus.PROCESSED, analysisScheduleInbox.getEntries().getFirst().status());
    assertEquals(1, visualizationAnalysisInbox.getEntries().size());
    assertEquals(
        EventProcessingStatus.PROCESSED,
        visualizationAnalysisInbox.getEntries().getFirst().status());
  }

  @Test
  void scheduleUseCaseShouldPublishThroughSpringAdapter() {
    MissionSchedule schedule = scheduleApplicationService.createSchedule("plan-1", 30);

    Threat threat = new Threat(ThreatType.DUST_STORM, "solar-panels", 1.5, 2, "days");
    threat.setId("threat-1");
    threat.setType(EventType.THREAT);
    threat.setSol(3);
    threat.setDescription("Dust storm");
    scheduleApplicationService.addEvent(schedule.getId(), threat);

    List<String> eventTypes =
        analysisScheduleInbox.getEntries().stream().map(entry -> entry.eventType()).toList();
    assertEquals(3, analysisScheduleInbox.getEntries().size());
    assertTrue(
        eventTypes.containsAll(
            List.of("MissionScheduleCreated", "ThreatScheduled", "MissionScheduleUpdated")));
  }

  @Test
  void shouldIgnoreDuplicateEventId() {
    MissionScheduleCreated event = MissionScheduleCreated.create("schedule-1", "1");

    eventPublisher.publishEvent(event);
    eventPublisher.publishEvent(event);

    assertEquals(1, analysisScheduleInbox.getEntries().size());
    assertEquals(
        event.envelope().eventId(), analysisScheduleInbox.getEntries().getFirst().eventId());
  }

  @Test
  void shouldLeaveFailedStatusWhenProcessorThrows() {
    AnalysisScheduleEventInbox inbox = new AnalysisScheduleEventInbox();
    AnalysisScheduleEventListener listener =
        new AnalysisScheduleEventListener(
            inbox,
            ignored -> {
              throw new IllegalStateException("processor failed");
            });
    MissionScheduleCreated event = MissionScheduleCreated.create("schedule-1", "1");

    assertThrows(IllegalStateException.class, () -> listener.onMissionScheduleCreated(event));

    assertEquals(1, inbox.getEntries().size());
    assertEquals(EventProcessingStatus.FAILED, inbox.getEntries().getFirst().status());
    assertEquals("processor failed", inbox.getEntries().getFirst().error());
  }
}
