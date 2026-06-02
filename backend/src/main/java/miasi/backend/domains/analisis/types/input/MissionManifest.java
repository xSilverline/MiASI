package miasi.backend.domains.analisis.types.input;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import miasi.backend.domains.analisis.types.crew.CrewGroup;
import miasi.backend.domains.analisis.types.schedule.Delivery;
import miasi.backend.domains.analisis.types.schedule.Threat;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
public class MissionManifest {
    UUID id;
    int durationSols;
    int rescueSols;
    float maxWeightSolZero;

    List<CrewGroup> crew;
    List<Module> catalog;
    List<Delivery> deliveries;
    List<Threat> threats;
}