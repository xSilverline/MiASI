package miasi.backend.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "Nie podano loginu") String login,
    @NotBlank(message = "Nie podano hasła") String password) {}
