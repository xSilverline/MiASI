//package miasi.backend.domains.analysis.infrastructure.in.web;
//
//import lombok.RequiredArgsConstructor;
//import miasi.backend.domains.analysis.application.port.in.RunSimulationCommand;
//import miasi.backend.domains.analysis.application.port.in.RunSimulationUseCase;
//import miasi.backend.domains.analysis.domain.simulation.SimulationAnalysisSession;
//import miasi.backend.domains.analysis.infrastructure.in.web.dto.SimulationRequestDto;
//import miasi.backend.domains.analysis.infrastructure.in.web.dto.SimulationResponseDto;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/analysis")
//@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:*")
//public class SimulationController {
//
//  // Kontroler rozmawia TYLKO z interfejsem (portem wejściowym), a nie z implementacją!
//  private final RunSimulationUseCase runSimulationUseCase;
//
//  @PostMapping("/simulate")
//  public ResponseEntity<SimulationResponseDto> runSimulation(
//      @RequestBody SimulationRequestDto request) {
//
//    // 1. Mapujemy DTO z zewnątrz na obiekt polecenia (Command) dla warstwy aplikacji
//    RunSimulationCommand command = new RunSimulationCommand(
//        request.missionPlanId(),
//        request.scheduleId()
//    );
//
//    // 2. Odpalamy logikę przez Port Wejściowy
//    SimulationAnalysisSession session = runSimulationUseCase.simulate(command);
//
//    // 3. Mapujemy bogaty obiekt domenowy na płaski DTO dla frontendu (pominęłam pełne mapowanie dla czytelności)
//    SimulationResponseDto response = SimulationResponseDto.fromDomain(session);
//
//    return ResponseEntity.ok(response);
//  }
//}