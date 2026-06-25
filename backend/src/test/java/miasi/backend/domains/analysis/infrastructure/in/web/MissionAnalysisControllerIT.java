package miasi.backend.domains.analysis.infrastructure.in.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class MissionAnalysisControllerIT {

  @Autowired private MockMvc mvc;

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
  void analysisFlowKeepsShortEndpointsAndSuccessResponses() throws Exception {
    String optimizeResponse =
        mvc.perform(
                MockMvcRequestBuilders.post("/api/analysis/optimize")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"missionPlanId\":0}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.configuration.optimalModules").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

    String payloadSessionId = JsonPath.read(optimizeResponse, "$.sessionId");

    String nominalResponse =
        mvc.perform(
                MockMvcRequestBuilders.post("/api/analysis/nominal")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(nominalRequest(payloadSessionId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.nominalVariant.type").value("IDEAL"))
            .andReturn()
            .getResponse()
            .getContentAsString();

    String nominalSessionId = JsonPath.read(nominalResponse, "$.sessionId");

    mvc.perform(
            MockMvcRequestBuilders.post("/api/analysis/scenarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(scenariosRequest(nominalSessionId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.idealVariant.type").value("IDEAL"))
        .andExpect(jsonPath("$.realVariant.type").value("REAL"));
  }

  private String nominalRequest(String payloadSessionId) {
    return """
        {
          "payloadSessionId": "%s",
          "customizedModules": [
            {
              "name": "default_laboratory",
              "status": "ACTIVE",
              "category": "UTILITY_MODULE",
              "weight": 2137.0,
              "resourceConsumption": [
                { "resourceType": "ENERGY", "quantity": 1.0 },
                { "resourceType": "WATER", "quantity": 1.0 }
              ],
              "resourceProduction": [
                { "resourceType": "FOOD", "quantity": 2.5 },
                { "resourceType": "OXYGEN", "quantity": 15.0 }
              ]
            }
          ],
          "customizedSupplies": [
            { "type": "FOOD", "amount": 200.0, "weight": 0.0 },
            { "type": "OXYGEN", "amount": 200.0, "weight": 0.0 },
            { "type": "WATER", "amount": 200.0, "weight": 0.0 },
            { "type": "ENERGY", "amount": 200.0, "weight": 0.0 }
          ]
        }
        """
        .formatted(payloadSessionId);
  }

  private String scenariosRequest(String nominalSessionId) {
    return """
        {
          "nominalSessionId": "%s",
          "scheduleId": "0"
        }
        """
        .formatted(nominalSessionId);
  }
}
