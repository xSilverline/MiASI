package miasi.backend.domains.analysis.domain.core;

import lombok.Value;

@Value
public class Resource {

  ResourceType type;
  float amount;
  float weight;

  public Resource(ResourceType type, float amount) {
    this.type = type;
    this.amount = amount;
    this.weight = amount * type.getWeightRatio();
  }

  public Resource withAmount(float newAmount) {
    return new Resource(this.type, newAmount);
  }

  public Resource copy() {
    return new Resource(this.type, this.amount);
  }
}