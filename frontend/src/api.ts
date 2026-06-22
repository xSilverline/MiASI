const API_BASE_URL = import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ?? "";

const endpoint = (path: string) => `${API_BASE_URL}${path}`;

export const apiRoutes = {
  auth: {
    login: endpoint("/api/auth/login"),
    verify: (sessionToken: string) => endpoint(`/api/auth/${sessionToken}/verify`),
    logout: (sessionToken: string) => endpoint(`/api/auth/${sessionToken}/logout`),
  },
  configuration: {
    defaultPlan: endpoint("/api/conf/default/plan"),
    missionPlan: (missionId: string) => endpoint(`/api/conf/${missionId}/plan`),
    plansCount: endpoint("/api/conf/plans-count"),
    moduleCatalog: endpoint("/api/conf/module-catalog"),
    resourceTypes: endpoint("/api/conf/resource-types"),
    moduleStates: endpoint("/api/conf/module-states"),
  },
  schedule: {
    create: endpoint("/api/schedule"),
    byId: (scheduleId: string) => endpoint(`/api/schedule/${scheduleId}`),
    timeline: (scheduleId: string, type?: EventType) =>
      endpoint(`/api/schedule/${scheduleId}/timeline${type ? `?type=${type}` : ""}`),
    events: (scheduleId: string) => endpoint(`/api/schedule/${scheduleId}/events`),
    scenario: endpoint("/api/schedule/scenario"),
    scenarioById: (draftId: string) => endpoint(`/api/schedule/scenario/${draftId}`),
    approveScenario: (draftId: string) => endpoint(`/api/schedule/scenario/${draftId}/approve`),
    approveScenarioIntoSchedule: (scheduleId: string, draftId: string) =>
      endpoint(`/api/schedule/${scheduleId}/scenario/${draftId}/approve`),
  },
} as const;

export const eventTypes = ["SUPPLY_DELIVERY", "THREAT", "MODULE_STATE_CHANGE"] as const;
export const threatTypes = [
  "DUST_STORM",
  "MODULE_FAILURE",
  "RESOURCE_LOSS",
  "PRODUCTION_DISRUPTION",
] as const;
export const difficultyLevels = ["LEVEL_I", "LEVEL_II", "LEVEL_III", "LEVEL_IV", "LEVEL_V"] as const;
export const moduleStates = ["ACTIVE", "PARTIALLY_DAMAGED", "DESTROYED", "INACTIVE"] as const;
export const deliveryItemTypes = ["RESOURCE", "MODULE"] as const;

export type EventType = (typeof eventTypes)[number];
export type ThreatType = (typeof threatTypes)[number];
export type DifficultyLevel = (typeof difficultyLevels)[number];
export type ModuleState = (typeof moduleStates)[number];
export type DeliveryItemType = (typeof deliveryItemTypes)[number];

export interface BasicResponse {
  status: string;
  message: string;
}

export interface ResourceAmount {
  resourceType: string;
  quantity: number;
}

export interface CrewProfile {
  name: string;
  population: number;
  optimalDemand?: Record<string, number>;
  minimalDemand?: Record<string, number>;
}

export interface ModuleTypeDto {
  name: string;
  resourceConsumption?: ResourceAmount[];
  resourceProduction?: ResourceAmount[];
}

export interface MissionModule {
  name: string;
  status: ModuleState | string;
  type?: ModuleTypeDto;
  weight: number;
}

export interface MissionPlan {
  crew: CrewProfile[];
  missionDurationSols: number;
  startingResources: ResourceAmount[];
  modules: MissionModule[];
  maxStartingWeight: number;
}

export interface ModuleCatalog {
  moduleList: MissionModule[];
  typeList: ModuleTypeDto[];
}

export interface DeliveryItem {
  itemId: string;
  itemType: DeliveryItemType;
  quantity: number;
  weight: number;
}

export interface DeliveryContent {
  items: DeliveryItem[];
  totalWeight: number;
}

export interface ScheduledEvent {
  id: string;
  type: EventType;
  sol: number;
  description: string;
  threatType?: ThreatType;
  affectedElement?: string;
  impactValue?: number;
  durationSols?: number;
  impactUnit?: string;
  content?: DeliveryContent;
  moduleId?: string;
  newState?: ModuleState;
}

export interface ScheduleResponse {
  id: string;
  missionPlanId: string;
  durationSols: number;
  status: string;
  events: ScheduledEvent[];
}

export interface TimelineResponse {
  eventsSortedBySol: ScheduledEvent[];
}

export interface ScenarioDraftResponse {
  id: string;
  missionPlanId: string;
  durationSols: number;
  mode: string;
  difficulty: DifficultyLevel;
  proposedEvents: ScheduledEvent[];
}

export interface CreateScheduleRequest {
  missionPlanId: string;
  durationSols: number;
}

export interface GenerateScenarioRequest extends CreateScheduleRequest {
  difficulty: DifficultyLevel;
}

export type ScheduledEventRequest = ScheduledEvent;

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const response = await fetch(url, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...init?.headers,
    },
  });

  const text = await response.text();
  const body = text ? JSON.parse(text) : undefined;

  if (!response.ok) {
    const message = body?.message ?? body?.error ?? response.statusText;
    throw new Error(`${response.status} ${message}`);
  }

  return body as T;
}

const postJson = <T>(url: string, body?: unknown) =>
  request<T>(url, {
    method: "POST",
    body: body === undefined ? undefined : JSON.stringify(body),
  });

export const api = {
  login: (login: string, password: string) =>
    postJson<BasicResponse>(apiRoutes.auth.login, { login, password }),
  verify: (sessionToken: string) => postJson<BasicResponse>(apiRoutes.auth.verify(sessionToken)),
  logout: (sessionToken: string) => postJson<BasicResponse>(apiRoutes.auth.logout(sessionToken)),
  getDefaultMissionPlan: () => request<MissionPlan>(apiRoutes.configuration.defaultPlan),
  getMissionPlan: (missionId: string) =>
    request<MissionPlan>(apiRoutes.configuration.missionPlan(missionId)),
  getPlansCount: () => request<BasicResponse>(apiRoutes.configuration.plansCount),
  getModuleCatalog: () => request<ModuleCatalog>(apiRoutes.configuration.moduleCatalog),
  getResourceTypes: () => request<string[]>(apiRoutes.configuration.resourceTypes),
  getModuleStates: () => request<ModuleState[]>(apiRoutes.configuration.moduleStates),
  createSchedule: (payload: CreateScheduleRequest) =>
    postJson<ScheduleResponse>(apiRoutes.schedule.create, payload),
  getSchedule: (scheduleId: string) => request<ScheduleResponse>(apiRoutes.schedule.byId(scheduleId)),
  getTimeline: (scheduleId: string, type?: EventType) =>
    request<TimelineResponse>(apiRoutes.schedule.timeline(scheduleId, type)),
  addEvent: (scheduleId: string, payload: ScheduledEventRequest) =>
    postJson<ScheduleResponse>(apiRoutes.schedule.events(scheduleId), payload),
  generateScenario: (payload: GenerateScenarioRequest) =>
    postJson<ScenarioDraftResponse>(apiRoutes.schedule.scenario, payload),
  getScenarioDraft: (draftId: string) =>
    request<ScenarioDraftResponse>(apiRoutes.schedule.scenarioById(draftId)),
  approveScenarioDraft: (draftId: string) =>
    postJson<ScheduleResponse>(apiRoutes.schedule.approveScenario(draftId)),
  approveScenarioIntoSchedule: (scheduleId: string, draftId: string) =>
    postJson<ScheduleResponse>(apiRoutes.schedule.approveScenarioIntoSchedule(scheduleId, draftId)),
};
