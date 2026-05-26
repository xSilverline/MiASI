package miasi.backend.api.jsons;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Nie podano loginu")
    String login,

    @NotBlank(message = "Nie podano hasła")
    String passwordHash
) {
}
