package miasi.backend.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import miasi.backend.adapter.in.web.dto.BasicResponseEntity;
import miasi.backend.adapter.in.web.dto.LoginRequest;
import miasi.backend.authorization.application.exception.InvalidSessionTokenException;
import miasi.backend.authorization.application.port.in.LoginUseCase;
import miasi.backend.authorization.application.port.in.LogoutUseCase;
import miasi.backend.authorization.application.port.in.VerifySessionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:*") // TODO: change when frontend ports are known
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@ApiResponses({
  @ApiResponse(responseCode = "200", description = "Operation successful"),
  @ApiResponse(responseCode = "400", description = "Invalid input data")
})
public class AuthController {

  private final LoginUseCase loginUseCase;
  private final VerifySessionUseCase verifySessionUseCase;
  private final LogoutUseCase logoutUseCase;

  @PostMapping("/login")
  @ApiResponse(responseCode = "401", description = "Unauthorized access")
  public ResponseEntity<BasicResponseEntity> login(@Valid @RequestBody LoginRequest request) {

    String token = loginUseCase.login(request.login(), request.password());
    return ResponseEntity.ok(BasicResponseEntity.success(token));
  }

  @PostMapping("/{sessionToken}/verify")
  @ApiResponse(responseCode = "401", description = "Unauthorized access")
  public ResponseEntity<BasicResponseEntity> tokenVerify(@PathVariable String sessionToken) {
    if (verifySessionUseCase.isAuthenticated(sessionToken)) {
      return ResponseEntity.ok(BasicResponseEntity.success("true"));
    }

    throw new InvalidSessionTokenException("Session expired or token is invalid");
  }

  @PostMapping("/{sessionToken}/logout")
  @ApiResponse(responseCode = "401", description = "Unauthorized access")
  public ResponseEntity<BasicResponseEntity> logout(@PathVariable String sessionToken) {
    logoutUseCase.logout(sessionToken);
    return ResponseEntity.ok(BasicResponseEntity.success("Session ended successfully"));
  }
}
