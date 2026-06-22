import { useState } from "react";
import { authAdapter } from "../infrastructure/AuthAdapter";

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
      await authAdapter.login(username, password);
      return true; // Sukces
    } catch (err: any) {
      setError(err.message || "Wystąpił błąd podczas logowania.");
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
