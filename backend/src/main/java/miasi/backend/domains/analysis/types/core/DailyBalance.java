package miasi.backend.domains.analysis.types.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analysis.types.ResourceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class DailyBalance {
  List<Resource> produced;
  List<Resource> consumed;

  public DailyBalance() {
    this.produced = new ArrayList<>();
    this.consumed = new ArrayList<>();
  }

  public void addProduction(Resource resource) {
    // find stock in produced by type:
    // if there is -> add amount
    // if not -> append

    if (this.produced == null) this.produced = new ArrayList<>();

    for (Resource r : this.produced) {
      if (r.getType() == resource.getType()) {
        r.setAmount(r.getAmount() + resource.getAmount());
        return;
      }
    }
    // If not found -> append
    this.produced.add(new Resource(resource.getType(), resource.getAmount()));
  }

  public void addConsumption(Resource resource) {
    //  find stock in consumed by type:
    // if there is -> add amount
    // if not -> append
    if (this.consumed == null) this.consumed = new ArrayList<>();

    for (Resource r : this.consumed) {
      if (r.getType() == resource.getType()) {
        r.setAmount(r.getAmount() + resource.getAmount());
        return;
      }
    }
    this.consumed.add(new Resource(resource.getType(), resource.getAmount()));
  }

  public List<Resource> applyTo(List<Resource> inventory) {
    // new_inventory = copy(inventory)
    // new_inventory: increase by produced and decrease by consumed

    // copy yesterday's state
    Map<ResourceType, Float> newInventoryMap = new EnumMap<>(ResourceType.class);
    if (inventory != null) {
      for (Resource res : inventory) {
        newInventoryMap.put(res.getType(), res.getAmount());
      }
    }

    // add today's production
    if (produced != null) {
      for (Resource res : produced) {
        newInventoryMap.merge(res.getType(), res.getAmount(), Float::sum);
      }
    }

    // 3. subtract today's consumption
    if (consumed != null) {
      for (Resource res : consumed) {
        newInventoryMap.merge(res.getType(), -res.getAmount(), Float::sum);
      }
    }

    return newInventoryMap.entrySet().stream()
        .map(entry -> new Resource(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}