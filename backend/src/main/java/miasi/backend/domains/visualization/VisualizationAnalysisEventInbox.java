package miasi.backend.domains.visualization;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class VisualizationAnalysisEventInbox {
  private final List<Object> receivedEvents = new CopyOnWriteArrayList<>();

  public void record(Object event) {
    receivedEvents.add(event);
  }

  public List<Object> getReceivedEvents() {
    return List.copyOf(receivedEvents);
  }

  public void clear() {
    receivedEvents.clear();
  }
}
