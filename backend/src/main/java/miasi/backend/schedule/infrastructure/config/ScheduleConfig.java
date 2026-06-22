package miasi.backend.schedule.infrastructure.config;

import miasi.backend.schedule.application.port.out.MissionScheduleRepositoryPort;
import miasi.backend.schedule.application.port.out.ScheduleEventPublisherPort;
import miasi.backend.schedule.application.service.ScheduleApplicationService;
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
