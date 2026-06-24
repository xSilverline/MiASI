package miasi.backend.api.config;

import miasi.backend.domains.analysis.AnalysisScheduleEventInbox;
import miasi.backend.domains.schedule.MissionPlanEventInbox;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventInboxConfig {

  @Bean
  public AnalysisScheduleEventInbox analysisScheduleEventInbox() {
    return new AnalysisScheduleEventInbox();
  }

  @Bean
  public MissionPlanEventInbox missionPlanEventInbox() {
    return new MissionPlanEventInbox();
  }
}
