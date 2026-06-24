package miasi.backend.api;

import com.jayway.jsonpath.JsonPath;
import miasi.backend.api.config.ConfService;
import miasi.backend.domains.configuration.enums.ModuleState;
import miasi.backend.domains.configuration.enums.ResourceType;
import miasi.backend.domains.configuration.missionPlan.MissionPlan;
import miasi.backend.domains.configuration.modules.Module;
import miasi.backend.domains.configuration.modules.ModuleCategory;
import miasi.backend.domains.schedule.EventEffect;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.enums.EventType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class ConfControllerIT {

  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private ConfService ctx;

  //Przywracanie plików testowego database do stanu początkowego
  @AfterEach
  void restoreDatabaseFiles(@Value("${database.path.hardcopy}") String hardCopy, @Value("${database.path.realdb}") String changedCopy) throws IOException {
    Path sourceDir = Path.of(hardCopy);
    Path targetDir = Path.of(changedCopy);

    try (Stream<Path> files = Files.walk(sourceDir)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".json"))
          .forEach(source -> {
            try {
              Path relative = sourceDir.relativize(source);
              Path target = targetDir.resolve(relative);

              Files.createDirectories(target.getParent());

              Files.copy(
                  source,
                  target,
                  StandardCopyOption.REPLACE_EXISTING
              );
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
    }
  }

  @Test
  void getDefaultMissionPlan() throws Exception {
    // Given
    String url = "/api/conf/default/plan";
    String expectedJson = objectMapper.writeValueAsString(new MissionPlan());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void getMissionPlan() throws Exception {
    // Given
    int id = 0;
    String url = "/api/conf/%d/plan".formatted(id);
    String expectedJson = objectMapper.writeValueAsString(ctx.getMissionPlan(id));

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void getModuleCatalog() throws Exception {
    // Given
    String url = "/api/conf/module-catalog";
    String expectedJson = objectMapper.writeValueAsString(ctx.getModuleCatalog());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void getEventCatalog() throws Exception {
    // Given
    String url = "/api/conf/event-catalog";
    String expectedJson = objectMapper.writeValueAsString(ctx.getEventCatalog());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void postEventDefinition() throws Exception {
    // Given
    EventDefinition event =
        new EventDefinition(
            null,
            "Custom threat",
            EventType.THREAT,
            "Custom user event",
            "solar-panels",
            "reduced solar energy production",
            List.of(
                new EventEffect(
                    "ENERGY",
                    -10.0,
                    "PERCENT",
                    "reduced solar energy production")));

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/conf/event-catalog")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(event))
    );

    // Then
    result
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.name").value(event.getName()))
        .andExpect(jsonPath("$.type").value(event.getType().name()))
        .andExpect(jsonPath("$.description").value(event.getDescription()))
        .andExpect(jsonPath("$.affectedElement").value(event.getAffectedElement()))
        .andExpect(jsonPath("$.consequence").value(event.getConsequence()))
        .andExpect(jsonPath("$.effects[0].target").value("ENERGY"))
        .andExpect(jsonPath("$.effects[0].value").value(-10.0));
  }

  @Test
  void updateEventDefinition() throws Exception {
    // Given
    EventDefinition original =
        new EventDefinition("event-to-update", "Original", EventType.THREAT, "Original event");
    ctx.addEventDefinition(original);

    EventDefinition updated =
        new EventDefinition(
            null,
            "Updated",
            EventType.THREAT,
            "Updated event",
            "food-production",
            "food production is interrupted",
            List.of(
                new EventEffect(
                    "FOOD",
                    -50.0,
                    "PERCENT",
                    "food production is interrupted")));

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.put("/api/conf/event-catalog/%s".formatted(original.getId()))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updated))
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(original.getId()))
        .andExpect(jsonPath("$.name").value(updated.getName()))
        .andExpect(jsonPath("$.type").value(updated.getType().name()))
        .andExpect(jsonPath("$.description").value(updated.getDescription()))
        .andExpect(jsonPath("$.affectedElement").value(updated.getAffectedElement()))
        .andExpect(jsonPath("$.consequence").value(updated.getConsequence()))
        .andExpect(jsonPath("$.effects[0].target").value("FOOD"))
        .andExpect(jsonPath("$.effects[0].value").value(-50.0));
  }

  @Test
  void deleteEventDefinition() throws Exception {
    // Given
    EventDefinition event =
        new EventDefinition("event-to-delete", "To delete", EventType.THREAT, "Temporary event");
    ctx.addEventDefinition(event);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.delete("/api/conf/event-catalog/%s".formatted(event.getId()))
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"));
  }

  @Test
  void postMissionPlan() throws Exception {
    // Given
    MissionPlan missionPlan = new MissionPlan();
    String requestJson =
        objectMapper.writeValueAsString(missionPlan);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/conf/plan")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
    );
    int id = Integer.parseInt(JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.message"));

    // Then
    result
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.status").value("success"));
    JSONAssert.assertEquals(
        objectMapper.writeValueAsString(missionPlan),
        objectMapper.writeValueAsString(ctx.getMissionPlan(id)),
        true
    );
  }

  @Test
  void postModule() throws Exception {
    // Given
    Module module = new Module();
    String requestJson =
        objectMapper.writeValueAsString(module);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/conf/module")
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
    );
    int id = Integer.parseInt(JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.message"));

    // Then
    result
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.status").value("success"));
    JSONAssert.assertEquals(
        objectMapper.writeValueAsString(module),
        objectMapper.writeValueAsString(ctx.getModuleCatalog().get(id)),
        true
    );
  }

  @Test
  void getResourceTypes() throws Exception {
    // Given
    String url = "/api/conf/resource-types";
    String expectedJson = objectMapper.writeValueAsString(ResourceType.values());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void getModuleStates() throws Exception {
    // Given
    String url = "/api/conf/module-states";
    String expectedJson = objectMapper.writeValueAsString(ModuleState.values());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void getModuleCategories() throws Exception {
    // Given
    String url = "/api/conf/module-categories";
    String expectedJson = objectMapper.writeValueAsString(ModuleCategory.values());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void getEventTypes() throws Exception {
    // Given
    String url = "/api/conf/event-types";
    String expectedJson = objectMapper.writeValueAsString(EventType.values());

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get(url)
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(content().json(expectedJson));
  }

  @Test
  void postMissionPlanWithOverride() throws Exception {
    // Given
    int overrideId = 0;

    MissionPlan originalPlan = ctx.getMissionPlan(overrideId);

    MissionPlan updatedPlan = new MissionPlan();
    String requestJson = objectMapper.writeValueAsString(updatedPlan);

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/conf/plan")
            .param("override", String.valueOf(overrideId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(requestJson)
    );

    int returnedId = Integer.parseInt(
        JsonPath.read(
            result.andReturn().getResponse().getContentAsString(),
            "$.message"
        )
    );

    // Then
    result
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value(String.valueOf(overrideId)));

    JSONAssert.assertEquals(
        objectMapper.writeValueAsString(updatedPlan),
        objectMapper.writeValueAsString(ctx.getMissionPlan(returnedId)),
        true
    );
  }


  @Test
  void getMissionsCount() throws Exception {
    // Given
    int expectedCount = ctx.getPlansCount();

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.get("/api/conf/plans-count")
    );

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").value(String.valueOf(expectedCount)));
  }

  @Test
  void postMissionPlanWithInvalidOverrideReturnsNotFound() throws Exception {
    // Given
    int invalidId = 999999;

    MissionPlan missionPlan = new MissionPlan();

    // When
    ResultActions result = mvc.perform(
        MockMvcRequestBuilders.post("/api/conf/plan")
            .param("override", String.valueOf(invalidId))
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(missionPlan))
    );

    // Then
    result
        .andExpect(status().isNotFound());
  }
}
