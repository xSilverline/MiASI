package miasi.backend.domains.schedule.infrastructure.in.web.dto;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import miasi.backend.domains.schedule.ScheduledEvent;

public record TimelineSolResponse(int sol, List<ScheduledEvent> events) {

  public static List<TimelineSolResponse> fromEvents(List<ScheduledEvent> events) {
    Map<Integer, List<ScheduledEvent>> bySol =
        events.stream().collect(Collectors.groupingBy(ScheduledEvent::getSol));

    return bySol.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .map(
            entry ->
                new TimelineSolResponse(
                    entry.getKey(),
                    entry.getValue().stream()
                        .sorted(Comparator.comparing(ScheduledEvent::getId))
                        .toList()))
        .toList();
  }

  public static List<TimelineSolResponse> fromTimelineEvents(List<ScheduledEvent> events) {
    int lastSol = events.stream().mapToInt(ScheduledEvent::getSol).max().orElse(1);
    Map<Integer, List<ScheduledEvent>> bySol =
        events.stream().collect(Collectors.groupingBy(ScheduledEvent::getSol));

    return IntStream.rangeClosed(1, lastSol)
        .mapToObj(
            sol ->
                new TimelineSolResponse(
                    sol,
                    bySol.getOrDefault(sol, List.of()).stream()
                        .sorted(Comparator.comparing(ScheduledEvent::getId))
                        .toList()))
        .toList();
  }
}
