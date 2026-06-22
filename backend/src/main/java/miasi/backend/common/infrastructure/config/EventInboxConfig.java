package miasi.backend.common.infrastructure.config;

import miasi.backend.analysis.domain.model.AnalysisScheduleEventInbox;
import miasi.backend.schedule.domain.model.MissionPlanEventInbox;
import miasi.backend.visualization.domain.model.VisualizationAnalysisEventInbox;
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
