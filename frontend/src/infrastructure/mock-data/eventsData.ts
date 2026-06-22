import type { EventData } from "../../core/domain/entities/event.ts";

export const MOCK_EVENTS: EventData[] = [
  {
    id: "ev-1",
    name: "Burza Piaskowa",
    type: "pogodowe",
    duration: 3,
    impacts: [
      {
        id: "imp-1",
        targetId: "woda",
        targetName: "Woda",
        targetType: "resource",
        actionType: "minus",
        value: 100,
      },
    ],
  },
];
