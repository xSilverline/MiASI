package miasi.backend.bootstrap;

import miasi.backend.configuration.application.ConfigurationApplicationService;
import miasi.backend.configuration.application.port.out.ConfigurationEventPublisherPort;
import miasi.backend.configuration.application.port.out.MissionPlanRepositoryPort;
import miasi.backend.configuration.application.port.out.ModuleRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfigurationApplicationConfig {

  @Bean
  public ConfigurationApplicationService configurationApplicationService(
      MissionPlanRepositoryPort missionPlansRepository,
      ModuleRepositoryPort moduleRepository,
      ConfigurationEventPublisherPort eventPublisher) {
    return new ConfigurationApplicationService(
        missionPlansRepository, moduleRepository, eventPublisher);
  }
}
