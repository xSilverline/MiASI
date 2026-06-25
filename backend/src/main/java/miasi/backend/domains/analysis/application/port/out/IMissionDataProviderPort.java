package miasi.backend.domains.analysis.application.port.out;

import java.util.List;
import miasi.backend.domains.analysis.domain.core.MissionManifest;
import miasi.backend.domains.analysis.domain.modules.Module;

public interface IMissionDataProviderPort {

  MissionManifest getMissionManifest(int missionPlanId);

  List<Module> getMissionModules(int missionPlanId);
}
