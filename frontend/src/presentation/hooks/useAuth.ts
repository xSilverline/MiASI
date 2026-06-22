import { useState } from "react";
import { authRepository } from "../../infrastructure/dependencyInjection/container";

export const useAuth = () => {
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  const authenticate = async (
    username: string,
    password: string,
  ): Promise<boolean> => {
    setIsLoading(true);
    setError(null);

    try {
      await authRepository.login(username, password);
      return true; // Sukces
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Wystąpił nieznany błąd podczas logowania.");
      }
      return false; // Błąd
    } finally {
      setIsLoading(false);
    }
  };

  return {
    authenticate,
    isLoading,
    error,
  };
};
