package miasi.backend.api.jsons;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import miasi.backend.domains.schedule.ScheduledEvent;

public record EventTimelineDayResponse(int sol, List<ScheduledEvent> events) {

  public static List<EventTimelineDayResponse> from(
      List<ScheduledEvent> events, int durationSols) {
    List<ScheduledEvent> safeEvents = events == null ? List.of() : events;
    int maxEventSol =
        safeEvents.stream().mapToInt(ScheduledEvent::getSol).max().orElse(0);
    int timelineDuration = Math.max(durationSols, maxEventSol);
    if (timelineDuration < 1) {
      return List.of();
    }

    Map<Integer, List<ScheduledEvent>> eventsBySol =
        safeEvents.stream()
            .collect(
                Collectors.groupingBy(
                    ScheduledEvent::getSol,
                    Collectors.collectingAndThen(
                        Collectors.toList(),
                        dayEvents ->
                            dayEvents.stream()
                                .sorted(Comparator.comparing(ScheduledEvent::getId))
                                .toList())));

    return IntStream.rangeClosed(1, timelineDuration)
        .mapToObj(
            sol -> new EventTimelineDayResponse(sol, eventsBySol.getOrDefault(sol, List.of())))
        .toList();
  }
}
