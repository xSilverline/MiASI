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
import java.util.Map;
import java.util.stream.Stream;
import miasi.backend.domains.schedule.EventDefinition;
import miasi.backend.domains.schedule.EventEffect;
import miasi.backend.domains.schedule.enums.EventType;
import org.junit.jupiter.api.AfterEach;
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
  void eventCatalog_shouldSupportFrontendCrudAndBatchFlow() throws Exception {
    mvc.perform(MockMvcRequestBuilders.get("/api/event-catalog/types"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0]").value(EventType.SUPPLY_DELIVERY.name()));

    EventDefinition event =
        new EventDefinition(
            null,
            "Frontend dust storm",
            EventType.THREAT,
            "Created from frontend",
            "solar-panels",
            "reduced solar energy production",
            List.of(new EventEffect("ENERGY", -10.0, "PERCENT", "lower energy production")));

    ResultActions created =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/event-catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)));

    created
        .andExpect(status().isCreated())
        .andExpect(
            header()
                .string("Location", org.hamcrest.Matchers.containsString("/api/event-catalog/")))
        .andExpect(jsonPath("$.id").isNotEmpty())
        .andExpect(jsonPath("$.affectedElement").value(event.getAffectedElement()))
        .andExpect(jsonPath("$.consequence").value(event.getConsequence()))
        .andExpect(jsonPath("$.effects[0].target").value("ENERGY"))
        .andExpect(jsonPath("$.effects[0].value").value(-10.0));

    String eventDefinitionId =
        JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$.id");

    mvc.perform(MockMvcRequestBuilders.get("/api/event-catalog"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '%s')]".formatted(eventDefinitionId)).exists());

    EventDefinition batchEvent =
        new EventDefinition(
            null,
            "Resupply",
            EventType.SUPPLY_DELIVERY,
            "Batch delivery",
            "warehouse",
            "food added",
            List.of(new EventEffect("FOOD", 20.0, "KG", "extra food")));

    mvc.perform(
            MockMvcRequestBuilders.post("/api/event-catalog/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(batchEvent))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").isNotEmpty())
        .andExpect(jsonPath("$[0].type").value(EventType.SUPPLY_DELIVERY.name()));

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
            MockMvcRequestBuilders.put("/api/event-catalog/%s".formatted(eventDefinitionId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(eventDefinitionId))
        .andExpect(jsonPath("$.consequence").value(updated.getConsequence()))
        .andExpect(jsonPath("$.effects[0].value").value(-20.0));

    mvc.perform(MockMvcRequestBuilders.delete("/api/event-catalog/%s".formatted(eventDefinitionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"));
  }

  @Test
  void timeline_shouldUseCatalogEventsAndExposeDeliveriesThreatsAndDailyTimeline()
      throws Exception {
    String threatDefinitionId =
        createEventDefinition(
            new EventDefinition(
                null,
                "Dust storm",
                EventType.THREAT,
                "Dust storm on solar panels",
                "solar-panels",
                "energy production reduced",
                List.of(new EventEffect("ENERGY", -15.0, "PERCENT", "lower power output"))));
    String deliveryDefinitionId =
        createEventDefinition(
            new EventDefinition(
                null,
                "Food delivery",
                EventType.SUPPLY_DELIVERY,
                "Food delivery lands",
                "warehouse",
                "food stock increased",
                List.of(new EventEffect("FOOD", 30.0, "KG", "extra food"))));

    ResultActions createdThreat =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/timeline/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        Map.of("sol", 15, "eventDefinitionId", threatDefinitionId))));

    createdThreat
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value(EventType.THREAT.name()))
        .andExpect(jsonPath("$.sol").value(15))
        .andExpect(jsonPath("$.effects[0].target").value("ENERGY"));

    String threatEventId =
        JsonPath.read(createdThreat.andReturn().getResponse().getContentAsString(), "$.id");

    mvc.perform(
            MockMvcRequestBuilders.post("/api/timeline/events/batch")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        List.of(Map.of("sol", 10, "eventDefinitionId", deliveryDefinitionId)))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].type").value(EventType.SUPPLY_DELIVERY.name()))
        .andExpect(jsonPath("$[0].sol").value(10));

    mvc.perform(MockMvcRequestBuilders.get("/api/timeline"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].sol").value(1))
        .andExpect(jsonPath("$[0].events").isEmpty())
        .andExpect(jsonPath("$[9].sol").value(10))
        .andExpect(jsonPath("$[9].events[0].type").value(EventType.SUPPLY_DELIVERY.name()))
        .andExpect(jsonPath("$[14].sol").value(15))
        .andExpect(jsonPath("$[14].events[0].type").value(EventType.THREAT.name()));

    mvc.perform(MockMvcRequestBuilders.get("/api/timeline/deliveries"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].sol").value(10))
        .andExpect(jsonPath("$[0].events[0].type").value(EventType.SUPPLY_DELIVERY.name()));

    mvc.perform(MockMvcRequestBuilders.get("/api/timeline/threats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].sol").value(15))
        .andExpect(jsonPath("$[0].events[0].id").value(threatEventId));

    mvc.perform(
            MockMvcRequestBuilders.delete(
                "/api/timeline/sols/15/events/%s".formatted(threatEventId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"));
  }

  private String createEventDefinition(EventDefinition event) throws Exception {
    ResultActions created =
        mvc.perform(
            MockMvcRequestBuilders.post("/api/event-catalog")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)));

    created.andExpect(status().isCreated());
    return JsonPath.read(created.andReturn().getResponse().getContentAsString(), "$.id");
  }
}
