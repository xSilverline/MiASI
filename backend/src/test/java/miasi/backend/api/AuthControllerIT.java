package miasi.backend.api;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import miasi.backend.api.jsons.LoginRequest;
import miasi.backend.domains.authorization.Authorization;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthControllerIT {
  @Autowired private MockMvc mvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private Authorization ctx;

  @Value("${database.filename.users}")
  private String jsonPath;

  private static String token = null;

  @Order(1)
  @Test
  void notLoginBadUser() throws Exception {
    // Given
    String url = "/api/auth/login";
    LoginRequest credentials = new LoginRequest("user", "543");

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)));

    // Then
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.message").exists());
  }

  @Order(2)
  @Test
  void loginGoodUser() throws Exception {
    // Given
    String url = "/api/auth/login";
    LoginRequest credentials = new LoginRequest("user", "123");

    // When
    ResultActions result =
        mvc.perform(
            MockMvcRequestBuilders.post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(credentials)));

    // Then
    MvcResult mvcResult =
        result
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("success"))
            .andExpect(jsonPath("$.message").exists())
            .andReturn();

    // Save token for future tests
    token =
        objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("message").asText();
  }

  @Order(3)
  @Test
  void tokenVerifyGood() throws Exception {
    // Given
    String url = "/api/auth/" + token + "/verify";

    // When
    ResultActions result = mvc.perform(MockMvcRequestBuilders.post(url));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").exists())
        .andReturn();
  }

  @Order(4)
  @Test
  void tokenVerifyBad() throws Exception {
    // Given
    String url = "/api/auth/6767/verify";

    // When
    ResultActions result = mvc.perform(MockMvcRequestBuilders.post(url));

    // Then
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.message").exists())
        .andReturn();
  }

  @Order(5)
  @Test
  void logoutBad() throws Exception {
    // Given
    String url = "/api/auth/6767/logout";

    // When
    ResultActions result = mvc.perform(MockMvcRequestBuilders.post(url));

    // Then
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.message").exists())
        .andReturn();
  }

  @Order(6)
  @Test
  void logoutGood() throws Exception {
    // Given
    String url = "/api/auth/" + token + "/logout";

    // When
    ResultActions result = mvc.perform(MockMvcRequestBuilders.post(url));

    // Then
    result
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("success"))
        .andExpect(jsonPath("$.message").exists())
        .andReturn();
  }

  @Order(7)
  @Test
  void logoutSecondTime() throws Exception {
    // Given
    String url = "/api/auth/" + token + "/logout";

    // When
    ResultActions result = mvc.perform(MockMvcRequestBuilders.post(url));

    // Then
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.message").exists())
        .andReturn();
  }

  @Order(8)
  @Test
  void tokenVerifyExpired() throws Exception {
    // Given
    String url = "/api/auth/" + token + "/verify";

    // When
    ResultActions result = mvc.perform(MockMvcRequestBuilders.post(url));

    // Then
    result
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value("error"))
        .andExpect(jsonPath("$.message").exists())
        .andReturn();
  }
}
