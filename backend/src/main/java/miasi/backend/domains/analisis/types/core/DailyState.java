package miasi.backend.domains.analisis.types.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import java.util.List;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DailyState {
    int sol;                  // numer dnia na osi czasu symulacji
    List<Resource> warehouse; // stan magazynu po uwzględnieniu dzisiejszego bilansu i dostaw
    DailyBalance balance;     // zarejestrowany dzisiejszy bilans netto (produkcja - zużycie)
}