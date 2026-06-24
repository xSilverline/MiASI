package miasi.backend.domains.analysis.domain.core;

import lombok.Value;

@Value
public class Resource {

  ResourceType type;
  float amount;      // current amount of resource
  float weight;      // weight [kg]

  // constructor that calculates weight based on amount and resource type
  public Resource(ResourceType type, float amount) {
    this.type = type;
    this.amount = amount;
    this.weight = amount * type.getWeightRatio();
  }

  // ZASTĘPSTWO DLA SETTERA:
  // Zwraca nową kopię z nową ilością i automatycznie przeliczoną wagą!
  public Resource withAmount(float newAmount) {
    return new Resource(this.type, newAmount);
  }

  public Resource copy() {
    return new Resource(this.type, this.amount);
  }
}