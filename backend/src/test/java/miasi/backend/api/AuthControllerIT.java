package miasi.backend.api;

import miasi.backend.domains.authorization.Authorization;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIT {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private Authorization ctx;

  @Value("${database.filename.users}")
  private String jsonPath;

  @Test
  void login() {
  }

  @Test
  void tokenVerify() {
  }

  @Test
  void logout() {
  }
}