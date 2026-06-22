package miasi.backend.common.infrastructure.in.web;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class ApiValidationErrorResponseTest {

  @Autowired private MockMvc mvc;

  @Test
  void shouldReturnStableValidationErrorForInvalidScheduleRequest() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "missionPlanId": "",
                      "durationSols": 0
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.message").value("Request validation failed"))
        .andExpect(jsonPath("$.details.missionPlanId").value("Mission plan id is required"))
        .andExpect(
            jsonPath("$.details.durationSols").value("Mission duration must be at least 1 sol"))
        .andExpect(jsonPath("$.path").value("/api/schedule"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturnStableValidationErrorForInvalidLoginRequest() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "login": "",
                      "password": ""
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
        .andExpect(jsonPath("$.details.login").value("Nie podano loginu"))
        .andExpect(jsonPath("$.details.password").value("Nie podano hasła"))
        .andExpect(jsonPath("$.path").value("/api/auth/login"));
  }

  @Test
  void shouldReturnStableMalformedJsonErrorForInvalidEnum() throws Exception {
    mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule/scenario")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "missionPlanId": "plan-1",
                      "durationSols": 10,
                      "difficulty": "IMPOSSIBLE"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
        .andExpect(jsonPath("$.message").value("Request body cannot be parsed"))
        .andExpect(jsonPath("$.path").value("/api/schedule/scenario"));
  }

  @Test
  void shouldReturnStableBadRequestForMissingSubtypePayload() throws Exception {
    String scheduleId = createSchedule();

    mvc.perform(
            MockMvcRequestBuilders.post("/api/schedule/%s/events".formatted(scheduleId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "id": "threat-1",
                      "type": "THREAT",
                      "sol": 2,
                      "description": "Dust storm"
                    }
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
        .andExpect(jsonPath("$.message").value("Threat type is required"))
        .andExpect(jsonPath("$.path").value("/api/schedule/" + scheduleId + "/events"));
  }

  private String createSchedule() throws Exception {
    String response =
        mvc.perform(
                MockMvcRequestBuilders.post("/api/schedule")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        {
                          "missionPlanId": "plan-1",
                          "durationSols": 10
                        }
                        """))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();

    int idStart = response.indexOf("\"id\":\"") + "\"id\":\"".length();
    int idEnd = response.indexOf('"', idStart);
    return response.substring(idStart, idEnd);
  }
}
