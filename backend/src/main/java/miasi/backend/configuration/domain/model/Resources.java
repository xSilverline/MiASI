package miasi.backend.configuration.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import miasi.backend.common.domain.model.ResourceType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Resources {
  private ResourceType resourceType;
  private float quantity;
}
