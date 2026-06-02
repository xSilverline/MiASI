export type TargetType = "module" | "resource";
export type ActionType = "efficiency" | "plus" | "minus";

export interface EventImpact {
  id: string;
  targetId: string;
  targetName: string;
  targetType: TargetType;
  actionType: ActionType;
  value: number;
}

export interface EventData {
  id: string;
  name: string;
  type: string;
  duration: number;
  impacts: EventImpact[];
}

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
