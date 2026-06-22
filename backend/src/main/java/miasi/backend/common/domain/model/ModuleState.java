package miasi.backend.common.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModuleState {
  ACTIVE(100),
  PARTIALLY_DAMAGED(50),
  DESTROYED(0),
  INACTIVE(0);

  private final int value;
}
