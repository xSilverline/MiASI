package miasi.backend.visualization.domain.model;

import java.util.List;
import java.util.function.Consumer;
import miasi.backend.common.domain.model.event.EventInboxEntry;
import miasi.backend.common.domain.model.event.InMemoryEventInbox;
import miasi.backend.common.domain.model.event.IntegrationEvent;

public class VisualizationAnalysisEventInbox {
  private final InMemoryEventInbox<IntegrationEvent> inbox = new InMemoryEventInbox<>();

  public boolean record(IntegrationEvent event) {
    return inbox.record(event);
  }

  public boolean handle(IntegrationEvent event, Consumer<IntegrationEvent> processor) {
    return inbox.handle(event, processor);
  }

  public List<IntegrationEvent> getReceivedEvents() {
    return inbox.events();
  }

  public List<EventInboxEntry<IntegrationEvent>> getEntries() {
    return inbox.entries();
  }

  public void clear() {
    inbox.clear();
  }
}
