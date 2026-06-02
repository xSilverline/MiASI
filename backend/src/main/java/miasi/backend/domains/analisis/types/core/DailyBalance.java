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
public class DailyBalance {
    List<Resource> produced;
    List<Resource> consumed;

    public void addProduction(Resource resource) {
        // znajdź surowiec w produced po typie:
            // jeśli jest -> dodaj amount
            // jeśli brak -> append
    }

    public void addConsumption(Resource resource) {
        // znajdź surowiec w consumed po typie:
            // jeśli jest -> dodaj amount
            // jeśli brak -> append
    }

    public List<Resource> applyTo(List<Resource> inventory) {
        // new_inventory = copy(inventory)
        // new_inventory: powiększ o produced i pomniejsz o consumed
        return inventory; // new_inventory
    }
}