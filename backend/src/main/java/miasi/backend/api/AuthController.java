package miasi.backend.api;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import miasi.backend.api.jsons.BasicResponseEntity;
import miasi.backend.api.jsons.LoginRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@CrossOrigin(origins = "http://localhost:*")// TODO: do zmiany gdy będą znane porty frontendu
@RestController
@RequestMapping("/api/auth")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Operacja zakończona sukcesem"),
    @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane wejściowe")
})
public class AuthController {
  @PostMapping("/login")
  @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
  public ResponseEntity<BasicResponseEntity> login(
      @RequestBody LoginRequest request
  ) {
    Random rnd = new Random();
    if (rnd.nextBoolean()) {// sprawdzanie "request" w bazie
      return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .body(BasicResponseEntity.error("Nieprawidłowy login lub hasło"));
    } else {
      return ResponseEntity
          .ok(BasicResponseEntity.success("Zalogowano " + request.login()));
    }
  }

  @PostMapping("/{sessionToken}/verify")
  @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
  public ResponseEntity<BasicResponseEntity> tokenVerify(
      @PathVariable String sessionToken
  ) {
    if (sessionToken.endsWith("a")) {// sprawdzanie "sessionToken"
      return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .body(BasicResponseEntity.error("Sesja wygasła lub token jest nieprawidłowy"));
    } else {
      return ResponseEntity
          .ok(BasicResponseEntity.success("true"));
    }
  }

  @PostMapping("/{sessionToken}/logout")
  @ApiResponse(responseCode = "401", description = "Nieautoryzowany dostęp")
  public ResponseEntity<BasicResponseEntity> logout(
      @PathVariable String sessionToken
  ) {
    if (sessionToken.endsWith("a")) {// wylogowywanie
      return ResponseEntity
          .status(HttpStatus.UNAUTHORIZED)
          .body(BasicResponseEntity.error("Sesja wygasła lub token jest nieprawidłowy"));
    } else {
      return ResponseEntity
          .ok(BasicResponseEntity.success("Sesja została zakonczona"));
    }
  }
}
