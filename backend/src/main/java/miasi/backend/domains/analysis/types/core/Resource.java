package miasi.backend.domains.analysis.types.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import miasi.backend.sharedkernel.model.ResourceType;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Resource {
  final ResourceType type;
  float amount; // current amount of resource
  float weight; // weight [kg]

  // constructor that calculates weight based on amount and resource type
  public Resource(ResourceType type, float amount) {
    this.type = type;
    this.amount = amount;
    this.weight = amount * type.getWeightRatio();
  }

  // Setter for amount that updates weight accordingly
  public void setAmount(float amount) {
    this.amount = amount;
    this.weight = this.amount * this.type.getWeightRatio();
  }

  public Resource copy() {
    return new Resource(this.type, this.amount);
  }
}
