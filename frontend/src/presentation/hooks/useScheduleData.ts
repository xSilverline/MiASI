import { useState, useEffect, useCallback } from "react";
import type { ScheduledEvent } from "../../core/domain/entities/event";
import { scheduleRepository } from "../../infrastructure/dependencyInjection/container";

export const useScheduleData = () => {
  const [scheduledEvents, setScheduledEvents] = useState<ScheduledEvent[]>([]);
  const [isLoading, setIsLoading] = useState<boolean>(true);

  const silentRefetch = useCallback(async () => {
    try {
      const data = await scheduleRepository.getEvents();
      setScheduledEvents(data);
    } catch (error) {
      console.error("Błąd pobierania harmonogramu:", error);
    }
  }, []);

  useEffect(() => {
    let isMounted = true;

    const loadInitialData = async () => {
      try {
        const data = await scheduleRepository.getEvents();
        if (isMounted) {
          setScheduledEvents(data);
        }
      } catch (error) {
        console.error("Błąd pobierania harmonogramu:", error);
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
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
    const eventToSave: ScheduledEvent = {
      ...eventData,
      id:
        editId ||
        `sched-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`,
    };

    setScheduledEvents((prev) =>
      editId
        ? prev.map((ev) => (ev.id === editId ? eventToSave : ev))
        : [...prev, eventToSave],
    );

    try {
      await scheduleRepository.saveEvent(eventToSave);
    } catch (error) {
      console.error("Błąd zapisu zdarzenia:", error);
      await silentRefetch();
    }
  };

  const deleteEvent = async (id: string) => {
    setScheduledEvents((prev) => prev.filter((ev) => ev.id !== id));
    try {
      await scheduleRepository.deleteEvent(id);
    } catch (error) {
      console.error("Błąd usuwania zdarzenia:", error);
      await silentRefetch();
    }
  };

  return { scheduledEvents, isLoading, saveEvent, deleteEvent };
};
