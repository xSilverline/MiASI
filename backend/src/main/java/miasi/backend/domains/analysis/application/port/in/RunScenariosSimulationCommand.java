package miasi.backend.domains.analysis.application.port.in;

public record RunScenariosSimulationCommand(
    String nominalSessionId, // ID zatwierdzonej Fazy 2 (nasz idealny układ)
    String scheduleId // ID harmonogramu (skąd weźmiemy Threats/Awarie)
    ) {}
