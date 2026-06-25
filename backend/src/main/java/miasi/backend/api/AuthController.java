package miasi.backend.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.api.jsons.LoginRequest;
import miasi.backend.domains.authorization.Authorization;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:5173") // TODO: change when frontend ports are known
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Operation successful"),
  @ApiResponse(responseCode = "400", description = "Invalid input data")
})
public class AuthController {

  private final Authorization authService;

  @PostMapping("/login")
  @ApiResponse(responseCode = "401", description = "Unauthorized access")
  public ResponseEntity<BasicResponseEntity> login(@RequestBody LoginRequest request) {

    try {
      // verify password and generate UUID
      String token = authService.login(request.login(), request.password());

      return ResponseEntity.ok(BasicResponseEntity.success(token));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(BasicResponseEntity.error(e.getMessage()));
    }
  }

  @PostMapping("/{sessionToken}/verify")
  @ApiResponse(responseCode = "401", description = "Unauthorized access")
  public ResponseEntity<BasicResponseEntity> tokenVerify(@PathVariable String sessionToken) {
    // ask if an active session with this token exists
    if (authService.isAuthenticated(sessionToken)) {
      return ResponseEntity.ok(BasicResponseEntity.success("true"));
    } else {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(BasicResponseEntity.error("Session expired or token is invalid"));
    }
  }

  @PostMapping("/{sessionToken}/logout")
  @ApiResponse(responseCode = "401", description = "Unauthorized access")
  public ResponseEntity<BasicResponseEntity> logout(@PathVariable String sessionToken) {
    try {
      // remove the session from RAM
      authService.logout(sessionToken);
      return ResponseEntity.ok(BasicResponseEntity.success("Session ended successfully"));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(BasicResponseEntity.error(e.getMessage()));
    }
  }
}
