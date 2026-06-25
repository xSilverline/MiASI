package miasi.backend.domains.analysis.domain.core;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
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
    if (this.produced == null) {
      this.produced = new ArrayList<>();
    }

    for (int i = 0; i < produced.size(); i++) {
      Resource r = produced.get(i);
      if (r.getType() == resource.getType()) {
        produced.set(i, r.withAmount(r.getAmount() + resource.getAmount()));
        return;
      }
    }
    this.produced.add(new Resource(resource.getType(), resource.getAmount()));
  }

  public void addConsumption(Resource resource) {
    if (this.consumed == null) {
      this.consumed = new ArrayList<>();
    }

    for (int i = 0; i < consumed.size(); i++) {
      Resource r = consumed.get(i);
      if (r.getType() == resource.getType()) {
        consumed.set(i, r.withAmount(r.getAmount() + resource.getAmount()));
        return;
      }
    }
    this.consumed.add(new Resource(resource.getType(), resource.getAmount()));
  }

  public List<Resource> applyTo(List<Resource> inventory) {
    Map<ResourceType, Float> newInventoryMap = new EnumMap<>(ResourceType.class);

    if (inventory != null) {
      for (Resource res : inventory) {
        newInventoryMap.put(res.getType(), res.getAmount());
      }
    }

    if (produced != null) {
      for (Resource res : produced) {
        newInventoryMap.merge(res.getType(), res.getAmount(), Float::sum);
      }
    }

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
