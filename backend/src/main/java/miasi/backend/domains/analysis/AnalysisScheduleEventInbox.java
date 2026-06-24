package miasi.backend.domains.analysis;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class AnalysisScheduleEventInbox {

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
