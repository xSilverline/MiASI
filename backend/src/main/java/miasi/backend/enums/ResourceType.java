package miasi.backend.enums;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public enum ResourceType {
  FOOD(0.5f),
  OXYGEN(1f),
  CARBON_DIOXIDE(1f),
  WATER(1f),
  ENERGY(0f);

  private final float weightRatio;

  public static ResourceType[] getDemandResourcesTypes() {
    return new ResourceType[]{FOOD, OXYGEN, WATER};
  }
}
