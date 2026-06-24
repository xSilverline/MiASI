package miasi.backend.domains.analysis.domain.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Resource {

  ResourceType type;
  float amount;
  float weight;

  // 1. Jawny konstruktor dla Jacksona, by wiedział jak czytać z pliku JSON
  @JsonCreator
  public Resource(
      @JsonProperty("type") ResourceType type,
      @JsonProperty("amount") float amount,
      @JsonProperty("weight") float weight) {
    this.type = type;
    this.amount = amount;
    this.weight = weight;
  }

  // 2. Twój dotychczasowy konstruktor biznesowy
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
