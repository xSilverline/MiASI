package miasi.backend.domains.analysis.types.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.types.crew.ConsumptionMode;
import miasi.backend.domains.analysis.types.modules.Module;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DailyState {
  int sol;                              // numer dnia na osi czasu symulacji
  List<Resource> warehouse;             // stan magazynu po uwzględnieniu dzisiejszego bilansu i dostaw
  DailyBalance balance;                 // zarejestrowany dzisiejszy bilans netto (produkcja - zużycie)
  ConsumptionMode mode;                 // Tryb konsumpcji aktywny w danym dniu
  List<Module> modules;                 // Stan maszyn w danym dniu (w tym ich statusy i sprawność)
  Set<ObservationType> observations;   // Specjalne wydarzenia czytelne dla frontendu - !!!
}