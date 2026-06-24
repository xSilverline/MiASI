package miasi.backend.domains.analysis.domain.core;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import miasi.backend.domains.analysis.domain.crew.CrewGroup;
import miasi.backend.domains.analysis.domain.schedule.Delivery;
import miasi.backend.domains.analysis.domain.schedule.Threat;

@Value
@Builder
@AllArgsConstructor
public class MissionManifest {

  int id;
  int durationSols;
  int rescueSols;
  float maxWeightSolZero;

  List<CrewGroup> crew;
  List<Delivery> deliveries;
  List<Threat> threats;

  public MissionManifest copyWithThreats(List<Threat> newThreats) {
    return new MissionManifest(
        this.id,
        this.durationSols,
        this.rescueSols,
        this.maxWeightSolZero,
        this.crew,
        this.deliveries,
        newThreats
    );
  }

  public MissionManifest copyWithDeliveries(List<Delivery> newDeliveries) {
    return new MissionManifest(
        this.id,
        this.durationSols,
        this.rescueSols,
        this.maxWeightSolZero,
        this.crew,
        newDeliveries,
        this.threats
    );
  }
}