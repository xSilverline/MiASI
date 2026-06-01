package miasi.backend.domains.configuration.other;

import lombok.*;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ResourceType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@RequiredArgsConstructor
public class Resources {
  final ResourceType resourceType;
  float quantity;
}
