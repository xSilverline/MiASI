package miasi.backend.api.config;

import miasi.backend.domains.analysis.AnalysisScheduleEventInbox;
import miasi.backend.domains.schedule.MissionPlanEventInbox;
import miasi.backend.domains.visualization.VisualizationAnalysisEventInbox;
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

  @Bean
  public VisualizationAnalysisEventInbox visualizationAnalysisEventInbox() {
    return new VisualizationAnalysisEventInbox();
  }
}
