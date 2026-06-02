package miasi.backend.domains.analisis.types.core;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.enums.ResourceType;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class Resource {
    ResourceType type;
    float amount;      // bieżąca ilość/objętość surowca
    float weight;      // waga przeliczona na kg
}