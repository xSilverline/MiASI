package miasi.backend.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResourceType {
  FOOD(0.5f),
  OXYGEN(1.2f),
  WATER(1.1f),
  ENERGY(0f);

  private final float weightRatio;

  public static ResourceType[] getDemandResourcesTypes() {
    return new ResourceType[] {FOOD, OXYGEN, WATER};
  }
}
