package miasi.backend.domains.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import miasi.backend.domains.configuration.enums.ResourceType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Resources {
  private ResourceType resourceType;
  private float quantity;
}
