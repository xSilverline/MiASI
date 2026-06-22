import type { IAuthRepository } from "../../core/application/ports/IAuthRepository";

export class ApiAuthAdapter implements IAuthRepository {
  private readonly API_URL = "http://localhost:8080/api";

  async login(username: string, password: string): Promise<void> {
    try {
      const response = await fetch(`${this.API_URL}/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          login: username,
          password: password,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Nieprawidłowy login lub hasło.");
      }
      const sessionToken = data.message;
      if (!sessionToken) {
        throw new Error("Brak tokena w odpowiedzi serwera.");
      }

      localStorage.setItem("sessionToken", sessionToken);
    } catch (error: unknown) {
      if (error instanceof Error) {
        throw error;
      }
      throw new Error(
        "Wystąpił błąd krytyczny podczas komunikacji z serwerem.",
      );
    }
  }
}
