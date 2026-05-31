package miasi.backend.domains.configuration.other;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class AtmosphereComposition {
  float oxygen;
  float carbonDioxide;
  float nitrogen;
}
