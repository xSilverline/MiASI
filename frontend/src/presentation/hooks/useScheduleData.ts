import { useState, useEffect, useCallback } from "react";
import type { ScheduledEvent } from "../../core/domain/entities/event";
import { scheduleRepository } from "../../infrastructure/dependencyInjection/container";

export const useScheduleData = () => {
  const [scheduledEvents, setScheduledEvents] = useState<ScheduledEvent[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const silentRefetch = useCallback(async () => {
    try {
      const data = await scheduleRepository.getEvents();
      setScheduledEvents(data);
      setError(null);
    } catch (err) {
      console.error("Błąd pobierania harmonogramu:", err);
      setError(
        err instanceof Error
          ? err.message
          : "Nie udało się pobrać harmonogramu.",
      );
    }
  }, []);

  useEffect(() => {
    let isMounted = true;

    const loadInitialData = async () => {
      setIsLoading(true);
      try {
        const data = await scheduleRepository.getEvents();
        if (isMounted) {
          setScheduledEvents(data);
          setError(null);
        }
      } catch (err) {
        console.error("Błąd pobierania harmonogramu:", err);
        if (isMounted) {
          setError(
            err instanceof Error
              ? err.message
              : "Nie udało się pobrać harmonogramu.",
          );
        }
      } finally {
        if (isMounted) setIsLoading(false);
      }
    };

    void loadInitialData();

    return () => {
      isMounted = false;
    };
  }, []);

  const saveEvent = async (
    eventData: Omit<ScheduledEvent, "id">,
    editId?: string,
  ) => {
    const optimisticId =
      editId || `sched-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;

    const eventToSave: ScheduledEvent = {
      ...eventData,
      id: optimisticId,
    };

    setScheduledEvents((prev) =>
      editId
        ? prev.map((ev) => (ev.id === editId ? eventToSave : ev))
        : [...prev, eventToSave],
    );

    try {
      if (editId) {
        // Timeline API has add/delete, but no update endpoint. For editing we
        // replace the scheduled occurrence and then refetch the authoritative id.
        await scheduleRepository.deleteEvent(editId);
      }

      await scheduleRepository.saveEvent(eventToSave);
      await silentRefetch();
    } catch (err) {
      console.error("Błąd zapisu zdarzenia:", err);
      await silentRefetch();
    }
  };

  const deleteEvent = async (id: string) => {
    setScheduledEvents((prev) => prev.filter((ev) => ev.id !== id));

    try {
      await scheduleRepository.deleteEvent(id);
      await silentRefetch();
    } catch (err) {
      console.error("Błąd usuwania zdarzenia:", err);
      await silentRefetch();
    }
  };

  return { scheduledEvents, isLoading, error, saveEvent, deleteEvent, refetch: silentRefetch };
};
