package miasi.backend.domains.configuration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import miasi.backend.domains.configuration.missionPlan.MissionPlansRepository;
import miasi.backend.domains.configuration.modules.ModuleCatalog;
import org.springframework.stereotype.Service;

@Getter
@Service
@RequiredArgsConstructor
public class ConfContextProvider {
  private final MissionPlansRepository repository;

  private final ModuleCatalog moduleCatalog;
}
