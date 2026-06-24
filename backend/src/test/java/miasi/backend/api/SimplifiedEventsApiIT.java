package miasi.backend.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;
import miasi.backend.domains.schedule.EventEffect;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.MissionSchedule;
import miasi.backend.domains.schedule.ScenarioDraft;
import miasi.backend.domains.schedule.ScheduleService;
import miasi.backend.domains.schedule.ScheduledEvent;
import miasi.backend.domains.schedule.enums.DifficultyLevel;
import miasi.backend.domains.schedule.enums.EventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
class SimplifiedEventsApiIT {

  @Autowired private MockMvc mvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private ScheduleService scheduleService;

  private MissionSchedule schedule;
  private ScenarioDraft draft;

  @BeforeEach
  void setUp() {
    schedule = scheduleService.createSchedule("0", 30);
    draft = scheduleService.generateScenario("0", 30, DifficultyLevel.LEVEL_I);
  }

  @AfterEach
  void restoreDatabaseFiles(
      @Value("${database.path.hardcopy}") String hardCopy,
      @Value("${database.path.realdb}") String changedCopy)
      throws IOException {
    Path sourceDir = Path.of(hardCopy);
    Path targetDir = Path.of(changedCopy);

    try (Stream<Path> files = Files.walk(sourceDir)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".json"))
          .forEach(
              source -> {
                try {
                  Path relative = sourceDir.relativize(source);
                  Path target = targetDir.resolve(relative);

                  Files.createDirectories(target.getParent());
                  Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                  throw new UncheckedIOException(e);
                }
              });
    }
  }

  @Test
  void getEvents_shouldReturnScheduleEventsForCalendar() throws Exception {
    ScheduledEvent event = new ScheduledEvent("calendar-event", EventType.THREAT, 3, "calendar");
    scheduleService.addEvent(schedule.getId(), event);

    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.get("/api/events")
                .param("context", "schedule")
                .param("contextId", schedule.getId()));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(event.getId()))
        .andExpect(jsonPath("$[0].type").value(event.getType().name()));
  }

  @Test
  void postAndDeleteEvents_shouldWorkForScheduleAndReturnUpdatedList() throws Exception {
    ScheduledEvent event =
        new ScheduledEvent("schedule-event", EventType.THREAT, 4, "schedule event");

    ResultActions added =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/events")
                .param("context", "schedule")
                .param("contextId", schedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)));

    added
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$[0].id").value(event.getId()));

    ResultActions deleted =
        mvc.perform(
            MockMvcRequestBuilders.delete("/api/events/%s".formatted(event.getId()))
                .param("context", "schedule")
                .param("contextId", schedule.getId()));

    deleted.andExpect(status().isOk()).andExpect(jsonPath("$").isEmpty());
  }

  @Test
  void postAndDeleteEvents_shouldWorkForScenarioAndReturnUpdatedList() throws Exception {
    ScheduledEvent event =
        new ScheduledEvent("scenario-event", EventType.THREAT, 5, "scenario event");

    ResultActions added =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/events")
                .param("context", "scenario")
                .param("contextId", draft.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)));

    added
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$[?(@.id == 'scenario-event')]").exists());

    ResultActions deleted =
        mvc.perform(
            MockMvcRequestBuilders.delete("/api/events/%s".formatted(event.getId()))
                .param("context", "scenario")
                .param("contextId", draft.getId()));

    deleted
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == 'scenario-event')]").isEmpty());
  }

  @Test
  void putEvents_shouldUpdateEventInSelectedContext() throws Exception {
    ScheduledEvent event = new ScheduledEvent("editable-event", EventType.THREAT, 7, "old");
    scheduleService.addEvent(schedule.getId(), event);
    ScheduledEvent updated = new ScheduledEvent("editable-event", EventType.THREAT, 8, "updated");

    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.put("/api/events/%s".formatted(event.getId()))
                .param("context", "schedule")
                .param("contextId", schedule.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)));

    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(updated.getId()))
        .andExpect(jsonPath("$[0].sol").value(updated.getSol()))
        .andExpect(jsonPath("$[0].description").value(updated.getDescription()));
  }

  @Test
  void eventDefinitions_shouldSupportFrontendCrudFlow() throws Exception {
    EventDefinition event =
        new EventDefinition(
            null,
            "Frontend dust storm",
            EventType.THREAT,
            "Created from frontend",
            "solar-panels",
            "reduced solar energy production",
            List.of(
                new EventEffect(
                    "ENERGY",
                    -10.0,
                    "PERCENT",
                    "reduced solar energy production")));

    ResultActions created =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/event-definitions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)));

    created
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.affectedElement").value(event.getAffectedElement()))
        .andExpect(jsonPath("$.consequence").value(event.getConsequence()))
        .andExpect(jsonPath("$.effects[0].target").value("ENERGY"))
        .andExpect(jsonPath("$.effects[0].value").value(-10.0));

    String eventDefinitionId =
        JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$.id");

    mvc.perform(MockMvcRequestBuilders.get("/api/event-definitions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(eventDefinitionId)).exists());

    EventDefinition updated =
        new EventDefinition(
            null,
            "Updated frontend dust storm",
            EventType.THREAT,
            "Updated from frontend",
            "solar-panels",
            "lower power output",
            List.of(new EventEffect("ENERGY", -20.0, "PERCENT", "lower power output")));

    mvc.perform(
            MockMvcRequestBuilders.put("/api/event-definitions/%s".formatted(eventDefinitionId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(eventDefinitionId))
        .andExpect(jsonPath("$.consequence").value(updated.getConsequence()))
        .andExpect(jsonPath("$.effects[0].value").value(-20.0));

    mvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/event-definitions/%s".formatted(eventDefinitionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"));
  }
}
