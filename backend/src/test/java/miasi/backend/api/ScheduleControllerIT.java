package miasi.backend.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import miasi.backend.adapter.in.web.dto.CreateScheduleRequest;
import miasi.backend.adapter.in.web.dto.GenerateScenarioRequest;
import miasi.backend.adapter.in.web.dto.ScheduleModuleStateChangeRequest;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.schedule.application.ScheduleApplicationService;
import miasi.backend.schedule.domain.DifficultyLevel;
import miasi.backend.schedule.domain.EventType;
import miasi.backend.sharedkernel.model.ModuleState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class ScheduleControllerIT {

  @Autowired private MockMvc mvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ScheduleApplicationService scheduleApplicationService;

  private MissionSchedule schedule;
  private ScenarioDraft draft;
  private ScheduledEvent event;

  @BeforeEach
  void setUp() {
    schedule = scheduleApplicationService.createSchedule("0", 10);
    event = new ScheduledEvent("0", EventType.THREAT, 9, "test");
    draft = scheduleApplicationService.generateScenario("0", 10, DifficultyLevel.LEVEL_I);
  }

  @Test
  void createSchedule() throws Exception {
    // Given
    CreateScheduleRequest request = new CreateScheduleRequest("0", 10);

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

    // Then
    result
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.missionPlanId").value("0"))
        .andExpect(jsonPath("$.durationSols").value(10))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.events").isArray());
  }

  @Test
  void getSchedule() throws Exception {
    // When
    ResultActions result =
        mvc.perform(MockMvcRequestBuilders.get("/api/schedule/" + schedule.getId()));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(schedule.getId()))
        .andExpect(jsonPath("$.missionPlanId").value("0"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.events").isArray());
  }

  @Test
  void getTimeline() throws Exception {
    // Given
    scheduleApplicationService.addEvent(
        schedule.getId(), new ScheduledEvent("later", EventType.THREAT, 9, "later"));
    scheduleApplicationService.addEvent(
        schedule.getId(), new ScheduledEvent("earlier", EventType.SUPPLY_DELIVERY, 2, "earlier"));

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.get("/api/schedule/%s/timeline".formatted(schedule.getId())));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eventsSortedBySol[0].id").value("earlier"))
        .andExpect(jsonPath("$.eventsSortedBySol[1].id").value("later"));
  }

  @Test
  void getTimelineFilteredByType() throws Exception {
    // Given
    scheduleApplicationService.addEvent(
        schedule.getId(), new ScheduledEvent("threat", EventType.THREAT, 7, "dust"));
    scheduleApplicationService.addEvent(
        schedule.getId(), new ScheduledEvent("supply", EventType.SUPPLY_DELIVERY, 7, "supply"));

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.get(
                "/api/schedule/%s/timeline?type=%s".formatted(schedule.getId(), EventType.THREAT)));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.eventsSortedBySol.length()").value(1))
        .andExpect(jsonPath("$.eventsSortedBySol[0].id").value("threat"))
        .andExpect(jsonPath("$.eventsSortedBySol[0].type").value("THREAT"));
  }

  @Test
  void addEvent() throws Exception {
    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule/%s/events".formatted(schedule.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(threatPayload("0", 9, "test")));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events.length()").value(1))
        .andExpect(jsonPath("$.events[0].id").value("0"))
        .andExpect(jsonPath("$.events[0].type").value("THREAT"))
        .andExpect(jsonPath("$.events[0].threatType").value("DUST_STORM"))
        .andExpect(jsonPath("$.events[0].affectedElement").value("solar-panels"))
        .andExpect(jsonPath("$.events[0].impactValue").value(1.5))
        .andExpect(jsonPath("$.events[0].durationSols").value(2))
        .andExpect(jsonPath("$.events[0].impactUnit").value("days"));
  }

  @Test
  void addSupplyDeliveryEventWithFullPayload() throws Exception {
    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule/%s/events".formatted(schedule.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(deliveryPayload("delivery-1", 4, "Resupply arrived")));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events.length()").value(1))
        .andExpect(jsonPath("$.events[0].id").value("delivery-1"))
        .andExpect(jsonPath("$.events[0].type").value("SUPPLY_DELIVERY"))
        .andExpect(jsonPath("$.events[0].content.totalWeight").value(42.5))
        .andExpect(jsonPath("$.events[0].content.items.length()").value(2))
        .andExpect(jsonPath("$.events[0].content.items[0].itemId").value("water-pack"))
        .andExpect(jsonPath("$.events[0].content.items[0].itemType").value("RESOURCE"))
        .andExpect(jsonPath("$.events[0].content.items[0].quantity").value(10.0))
        .andExpect(jsonPath("$.events[0].content.items[0].weight").value(2.5))
        .andExpect(jsonPath("$.events[0].content.items[1].itemId").value("habitat-spare"))
        .andExpect(jsonPath("$.events[0].content.items[1].itemType").value("MODULE"));
  }

  @Test
  void addModuleStateChangeEventWithFullPayload() throws Exception {
    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule/%s/events".formatted(schedule.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(moduleStateChangePayload("state-change-1", 5, "Module degraded")));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events.length()").value(1))
        .andExpect(jsonPath("$.events[0].id").value("state-change-1"))
        .andExpect(jsonPath("$.events[0].type").value("MODULE_STATE_CHANGE"))
        .andExpect(jsonPath("$.events[0].moduleId").value("habitat-1"))
        .andExpect(jsonPath("$.events[0].newState").value("PARTIALLY_DAMAGED"));
  }

  @Test
  void scheduleModuleStateChange() throws Exception {
    // Given
    ScheduleModuleStateChangeRequest request =
        new ScheduleModuleStateChangeRequest(
            null,
            3,
            "Habitat module partially damaged",
            "habitat-1",
            ModuleState.PARTIALLY_DAMAGED);

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post(
                    "/api/schedule/%s/module-state-changes".formatted(schedule.getId()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events.length()").value(1))
        .andExpect(jsonPath("$.events[0].type").value("MODULE_STATE_CHANGE"))
        .andExpect(jsonPath("$.events[0].moduleId").value("habitat-1"))
        .andExpect(jsonPath("$.events[0].newState").value("PARTIALLY_DAMAGED"));
  }

  @Test
  void removeEvent() throws Exception {
    // Given
    schedule = scheduleApplicationService.addEvent(schedule.getId(), event);
    String eventId = schedule.getEvents().getFirst().getId();

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/schedule/%s/events/%s".formatted(schedule.getId(), eventId)));

    // Then
    result.andExpect(status().isOk()).andExpect(jsonPath("$.status").value("success"));
  }

  @Test
  void generateScenario() throws Exception {
    // Given
    GenerateScenarioRequest request = new GenerateScenarioRequest("0", 10, DifficultyLevel.LEVEL_I);

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule/scenario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

    // Then
    result
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.missionPlanId").value("0"))
        .andExpect(jsonPath("$.proposedEvents").isArray());
  }

  @Test
  void getScenarioDraft() throws Exception {
    // When
    ResultActions result =
        mvc.perform(MockMvcRequestBuilders.get("/api/schedule/scenario/" + draft.getId()));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(draft.getId()))
        .andExpect(jsonPath("$.missionPlanId").value("0"))
        .andExpect(jsonPath("$.proposedEvents").isArray());
  }

  @Test
  void approveScenarioDraft() throws Exception {
    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post(
                "/api/schedule/scenario/%s/approve".formatted(draft.getId())));

    // Then
    result
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.status").value("READY_FOR_ANALYSIS"));
  }

  @Test
  void approveScenarioIntoSchedule() throws Exception {
    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post(
                "/api/schedule/%s/scenario/%s/approve".formatted(schedule.getId(), draft.getId())));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(schedule.getId()))
        .andExpect(jsonPath("$.status").value("READY_FOR_ANALYSIS"));
  }

  @Test
  void updateEvent() throws Exception {
    // Given
    String scheduleId = schedule.getId();
    schedule = scheduleApplicationService.addEvent(scheduleId, event);
    String eventId = schedule.getEvents().getFirst().getId();

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.put("/api/schedule/%s/events/%s".formatted(scheduleId, eventId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(threatPayload("1", 3, "updated")));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.events.length()").value(1))
        .andExpect(jsonPath("$.events[0].id").value("1"))
        .andExpect(jsonPath("$.events[0].sol").value(3))
        .andExpect(jsonPath("$.events[0].description").value("updated"))
        .andExpect(jsonPath("$.events[0].threatType").value("DUST_STORM"));
  }

  @Test
  void correctScenarioEvent() throws Exception {
    // Given
    String eventId = draft.getProposedEvents().getFirst().getId();

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.put(
                    "/api/schedule/scenario/%s/events/%s".formatted(draft.getId(), eventId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(threatPayload(eventId, 20, "corrected")));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.proposedEvents[0].id").value(eventId))
        .andExpect(jsonPath("$.proposedEvents[0].sol").value(20))
        .andExpect(jsonPath("$.proposedEvents[0].description").value("corrected"))
        .andExpect(jsonPath("$.proposedEvents[0].threatType").value("DUST_STORM"));
  }

  private String threatPayload(String id, int sol, String description) {
    return """
        {
          "id": "%s",
          "type": "THREAT",
          "sol": %d,
          "description": "%s",
          "threatType": "DUST_STORM",
          "affectedElement": "solar-panels",
          "impactValue": 1.5,
          "durationSols": 2,
          "impactUnit": "days"
        }
        """
        .formatted(id, sol, description);
  }

  private String deliveryPayload(String id, int sol, String description) {
    return """
        {
          "id": "%s",
          "type": "SUPPLY_DELIVERY",
          "sol": %d,
          "description": "%s",
          "content": {
            "totalWeight": 42.5,
            "items": [
              {
                "itemId": "water-pack",
                "itemType": "RESOURCE",
                "quantity": 10.0,
                "weight": 2.5
              },
              {
                "itemId": "habitat-spare",
                "itemType": "MODULE",
                "quantity": 1.0,
                "weight": 17.5
              }
            ]
          }
        }
        """
        .formatted(id, sol, description);
  }

  private String moduleStateChangePayload(String id, int sol, String description) {
    return """
        {
          "id": "%s",
          "type": "MODULE_STATE_CHANGE",
          "sol": %d,
          "description": "%s",
          "moduleId": "habitat-1",
          "newState": "PARTIALLY_DAMAGED"
        }
        """
        .formatted(id, sol, description);
  }
}
