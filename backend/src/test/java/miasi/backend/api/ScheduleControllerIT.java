package miasi.backend.api;

import com.jayway.jsonpath.JsonPath;
import miasi.backend.api.jsons.CreateScheduleRequest;
import miasi.backend.api.jsons.GenerateScenarioRequest;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.MissionTimeline;
import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.domains.schedule.ScheduleService;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.enums.DifficultyLevel;
import miasi.backend.enums.EventType;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class ScheduleControllerIT {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ScheduleService scheduleService;

  private MissionSchedule schedule;
  private ScenarioDraft draft;
  private ScheduledEvent event;

  @BeforeEach
  void setUp() {
    // Given - before each test
    schedule = scheduleService.createSchedule("0", 10);

    event = new ScheduledEvent("0", EventType.THREAT, 9, "test");

    draft = scheduleService.generateScenario("0", 10, DifficultyLevel.LEVEL_I);
  }

  @Test
  void createSchedule() throws Exception {
    // Given
    CreateScheduleRequest request = new CreateScheduleRequest("0", 10);
    String json = objectMapper.writeValueAsString(request);

    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/schedule")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
    );

    // When
    String response = result.andReturn().getResponse().getContentAsString();
    String id = JsonPath.read(response, "$.id");

    // Then
    result.andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(
            content().json(objectMapper.writeValueAsString(scheduleService.getSchedule(id)))
        );
  }


  @Test
  void getSchedule() throws Exception {

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get("/api/schedule/" + schedule.getId())
    );
    // Then
    result.andExpect(status().isOk())
        .andExpect(content().json(
            objectMapper.writeValueAsString(schedule)
        ));
  }

  @Test
  void getTimeline() throws Exception {
    // Given
    MissionTimeline timeline = scheduleService.getTimeline(schedule.getId());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get("/api/schedule/%s/timeline"
            .formatted(schedule.getId()))
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(
            objectMapper.writeValueAsString(timeline)
        ));
  }

  @Test
  void getTimelineFilteredByType() throws Exception {
    // Given
    EventType type = EventType.values()[0];
    MissionTimeline timeline = scheduleService.getTimeline(schedule.getId()).filterByType(type);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get("/api/schedule/%s/timeline?type=%s"
            .formatted(schedule.getId(), type))
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(
            objectMapper.writeValueAsString(timeline)
        ));
  }


  @Test
  void addEvent() throws Exception {
    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/schedule/%s/events"
                .formatted(schedule.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(event))
    );

    // Then
    result.andExpect(status().isOk());
  }


  @Test
  void removeEvent() throws Exception {
    //Given
    schedule = scheduleService.addEvent(schedule.getId(), event);
    String eventId = schedule.getEvents().getFirst().getId();

    //When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.delete("/api/schedule/%s/events/%s"
            .formatted(schedule.getId(), eventId))
    );

    // Then
    result.andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"));
  }

  @Test
  void generateScenario() throws Exception {
    // Given
    GenerateScenarioRequest request =
        new GenerateScenarioRequest("0", 10, DifficultyLevel.LEVEL_I);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/schedule/scenario")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
    );

    // Then
    result.andExpect(status().isCreated())
        .andExpect(header().exists("Location"));
  }

  @Test
  void getScenarioDraft() throws Exception {
    // Given
    ScenarioDraft draft = scheduleService.generateScenario("0", 10, DifficultyLevel.LEVEL_I);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get("/api/schedule/scenario/" + draft.getId())
    );

    // Then
    result.andExpect(status().isOk())
        .andExpect(content().json(
            objectMapper.writeValueAsString(draft)
        ));
  }

  @Test
  void approveScenarioDraft() throws Exception {
    // Given
    ScenarioDraft draft = scheduleService.generateScenario("0", 10, DifficultyLevel.LEVEL_I);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/schedule/scenario/%s/approve"
            .formatted(draft.getId()))
    );

    // Then
    result.andExpect(status().isCreated())
        .andExpect(header().exists("Location"));
  }

  @Test
  void approveScenarioIntoSchedule() throws Exception {
    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/schedule/%s/scenario/%s/approve"
            .formatted(schedule.getId(), draft.getId())
        )
    );

    // Then
    result.andExpect(status().isOk());
  }

  @Test
  void updateEvent() throws Exception {
    // Given
    String scheduleId = schedule.getId();
    schedule = scheduleService.addEvent(scheduleId, event);
    String eventId = schedule.getEvents().getFirst().getId();
    ScheduledEvent updatedEvent = new ScheduledEvent("1", EventType.THREAT, 3, "updated");

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.put("/api/schedule/%s/events/%s"
                .formatted(scheduleId, eventId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updatedEvent))
    );

    // Then
    result.andExpect(status().isOk());

    org.skyscreamer.jsonassert.JSONAssert.assertEquals(
        objectMapper.writeValueAsString(updatedEvent),
        objectMapper.writeValueAsString(scheduleService.getSchedule(scheduleId).getEvents().getFirst()),
        true
    );
  }

  @Test
  void correctScenarioEvent() throws Exception {
    // Given
    String eventId = draft.getProposedEvents().getFirst().getId();
    ScheduledEvent correctedEvent = new ScheduledEvent(eventId, EventType.THREAT, 20, "corrected");

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.put("/api/schedule/scenario/%s/events/%s"
                .formatted(draft.getId(), eventId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(correctedEvent))
    );

    // Then
    result.andExpect(status().isOk())
        .andExpect(content().json(
            objectMapper.writeValueAsString(
                scheduleService.correctScenarioEvent(draft.getId(), eventId, correctedEvent)
            )
        ));
  }
}