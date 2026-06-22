package miasi.backend.api.config;

import miasi.backend.domains.schedule.ScheduleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScheduleConfig {

  @Bean
  public ScheduleService scheduleService() {
    return new ScheduleService();
  }
}
