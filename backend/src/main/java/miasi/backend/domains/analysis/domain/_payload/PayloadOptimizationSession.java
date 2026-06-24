package miasi.backend.domains.analysis.domain._payload;

import java.time.LocalDateTime;
import lombok.Value;
import miasi.backend.domains.analysis.domain.core.MissionManifest;

@Value
public class PayloadOptimizationSession {

  String id;                           // Unikalne UUID
  MissionManifest inputManifest;       // WEJŚCIE: Zrzut danych, dla których to policzyliśmy
  OptimalConfiguration configuration;  // WYNIK: Wyliczona najlżejsza konfiguracja
  LocalDateTime createdAt;             // Kiedy wygenerowano
}