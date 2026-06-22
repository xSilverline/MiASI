package miasi.backend.api.config;

import miasi.backend.schedule.application.ScheduleApplicationService;
import miasi.backend.schedule.application.port.out.MissionScheduleRepositoryPort;
import miasi.backend.schedule.application.port.out.ScheduleEventPublisherPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleConfig {

  @Bean
  public ScheduleApplicationService scheduleApplicationService(
      MissionScheduleRepositoryPort scheduleRepository, ScheduleEventPublisherPort eventPublisher) {
    return new ScheduleApplicationService(scheduleRepository, eventPublisher);
  }
}
