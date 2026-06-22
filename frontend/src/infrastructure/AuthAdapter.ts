export interface IAuthRepository {
  login(username: string, password: string): Promise<void>;
}

export class MockAuthAdapter implements IAuthRepository {
  async login(username: string, password: string): Promise<void> {
    return new Promise((resolve, reject) => {
      setTimeout(() => {
        if (username.length > 0 && password.length > 0) {
          if (password === "error") {
            reject(new Error("Nieprawidłowy login lub hasło."));
          } else {
            resolve();
          }
        } else {
          reject(new Error("Brak danych logowania."));
        }
      }, 800);
    });
  }
}

export const authAdapter = new MockAuthAdapter();
