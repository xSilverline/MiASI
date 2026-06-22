import React, { useState } from "react";
import { User, Loader2, AlertCircle } from "lucide-react";
import marsIcon from "../assets/mars.png";
import earthBg from "../assets/earth.png";
import marsBg from "../assets/mars.png";

import { useAuth } from "../hooks/useAuth.ts";

interface LoginViewProps {
  onLogin: () => void;
}

export const LoginView: React.FC<LoginViewProps> = ({ onLogin }) => {
  const [login, setLogin] = useState("JanKowalski67");
  const [password, setPassword] = useState("admin123");

  const { authenticate, isLoading, error } = useAuth();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const isSuccess = await authenticate(login, password);

    if (isSuccess) {
      onLogin();
    }
  };

  return (
    <div className="h-screen w-screen bg-mars-background font-sans flex flex-col items-center justify-center relative overflow-hidden text-slate-100 select-none">
      {/* TŁA */}
      <div className="absolute left-0 top-1/2 -translate-x-1/2 -translate-y-1/2 w-[45vw] h-[90vh] opacity-40 pointer-events-none bg-radial from-blue-900/20 to-transparent rounded-full blur-2xl lg:opacity-100" />
      <img
        src={earthBg}
        className="absolute left-0 top-1/2 -translate-x-1/2 -translate-y-1/2 h-[90vh] w-auto object-contain pointer-events-none select-none hidden lg:block opacity-25"
        alt="Earth"
      />

      <div className="absolute right-0 top-1/2 translate-x-1/2 -translate-y-1/2 w-[45vw] h-[90vh] opacity-40 pointer-events-none bg-radial from-mars-orange/10 to-transparent rounded-full blur-2xl lg:opacity-100" />
      <img
        src={marsBg}
        className="absolute right-0 top-1/2 translate-x-1/2 -translate-y-1/2 h-[90vh] w-auto object-contain pointer-events-none select-none hidden lg:block opacity-25"
        alt="Mars"
      />

      <div className="flex flex-col items-center z-10 w-full max-w-md px-4">
        <div className="flex items-center gap-3 mb-8">
          <img
            src={marsIcon}
            alt="Mars Icon"
            className="w-15 h-15 object-contain shrink-0"
          />
          <h1 className="text-3xl font-bold text-gradient-mars tracking-wider uppercase">
            Misja Mars
          </h1>
        </div>

        <div className="bg-mars-itemBackground rounded-3xl shadow-xl w-full flex flex-col items-center gap-6 mb-8">
          <div className="w-full bg-mars-itemBackground py-5 rounded-xl flex items-center justify-center gap-3 text-base tracking-widest text-slate-100 font-medium shadow-inner">
            <User size={32} className="text-slate-100" />
            LOGOWANIE
          </div>
        </div>

        <div className="bg-mars-itemBackground p-10 rounded-3xl shadow-xl w-full flex flex-col items-center gap-6">
          <form
            onSubmit={handleSubmit}
            className="w-full flex flex-col gap-8 mt-2"
          >
            <div className="flex flex-col items-center">
              <label className="text-mars-orange text-base tracking-widest mb-2 uppercase">
                Login
              </label>
              <input
                type="text"
                value={login}
                onChange={(e) => setLogin(e.target.value)}
                disabled={isLoading}
                className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-xs tracking-wide focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all font-medium shadow-inner disabled:opacity-50"
                required
              />
            </div>

            <div className="flex flex-col items-center">
              <label className="text-mars-orange text-base tracking-widest mb-2 uppercase">
                Hasło
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={isLoading}
                className="w-full bg-mars-line text-white px-4 py-3 rounded-xl text-center text-xs tracking-widest focus:outline-none focus:ring-1 focus:ring-mars-orange/40 transition-all font-medium shadow-inner disabled:opacity-50"
                required
              />
            </div>

            {error && (
              <div className="w-full flex items-center justify-center gap-2 text-red-500 text-xs font-bold tracking-widest uppercase animate-in fade-in">
                <AlertCircle size={16} />
                <span>{error}</span>
              </div>
            )}

            <button
              type="submit"
              disabled={isLoading}
              className="flex items-center justify-center gap-3 hover:text-slate-200 text-gradient-mars font-bold text-lg py-3.5 px-10 self-center uppercase tracking-widest active:scale-95 transition-all disabled:opacity-50 disabled:cursor-not-allowed mt-4"
            >
              {isLoading ? (
                <>
                  <Loader2
                    size={24}
                    className="animate-spin text-mars-orange"
                  />
                  <span>Logowanie...</span>
                </>
              ) : (
                <span>Zaloguj</span>
              )}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
