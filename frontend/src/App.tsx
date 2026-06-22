import type { FormEvent } from "react";
import { useMemo, useState } from "react";
import {
  api,
  deliveryItemTypes,
  difficultyLevels,
  eventTypes,
  moduleStates,
  threatTypes,
  type DeliveryItemType,
  type DifficultyLevel,
  type EventType,
  type MissionPlan,
  type ModuleCatalog,
  type ModuleState,
  type ScenarioDraftResponse,
  type ScheduleResponse,
  type ScheduledEvent,
  type ThreatType,
} from "./api";
import "./App.css";

type View = "session" | "mission" | "catalog" | "schedule";

interface EventFormState {
  id: string;
  type: EventType;
  sol: number;
  description: string;
  threatType: ThreatType;
  affectedElement: string;
  impactValue: number;
  durationSols: number;
  impactUnit: string;
  itemId: string;
  itemType: DeliveryItemType;
  quantity: number;
  weight: number;
  totalWeight: number;
  moduleId: string;
  newState: ModuleState;
}

const initialEventForm: EventFormState = {
  id: "manual-event-1",
  type: "THREAT",
  sol: 1,
  description: "Operational event",
  threatType: "DUST_STORM",
  affectedElement: "habitat",
  impactValue: 10,
  durationSols: 1,
  impactUnit: "percent",
  itemId: "water",
  itemType: "RESOURCE",
  quantity: 25,
  weight: 25,
  totalWeight: 25,
  moduleId: "default_laboratory",
  newState: "PARTIALLY_DAMAGED",
};

const label = (value: string) =>
  value
    .toLowerCase()
    .split("_")
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");

const eventSummary = (event: ScheduledEvent) => {
  if (event.type === "THREAT") {
    return `${label(event.threatType ?? "THREAT")} on ${event.affectedElement ?? "unknown"}`;
  }
  if (event.type === "SUPPLY_DELIVERY") {
    return `${event.content?.items.length ?? 0} items, ${event.content?.totalWeight ?? 0} kg`;
  }
  return `${event.moduleId ?? "module"} -> ${label(event.newState ?? "UNKNOWN")}`;
};

const sortEvents = (events: ScheduledEvent[]) =>
  [...events].sort((left, right) => left.sol - right.sol || left.id.localeCompare(right.id));

function App() {
  const [view, setView] = useState<View>("session");
  const [busy, setBusy] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);

  const [login, setLogin] = useState("admin");
  const [password, setPassword] = useState("admin");
  const [sessionToken, setSessionToken] = useState("");

  const [missionId, setMissionId] = useState("0");
  const [plansCount, setPlansCount] = useState<string | null>(null);
  const [missionPlan, setMissionPlan] = useState<MissionPlan | null>(null);
  const [resourceTypes, setResourceTypes] = useState<string[]>([]);
  const [catalog, setCatalog] = useState<ModuleCatalog | null>(null);

  const [scheduleId, setScheduleId] = useState("");
  const [scheduleMissionId, setScheduleMissionId] = useState("0");
  const [durationSols, setDurationSols] = useState(30);
  const [schedule, setSchedule] = useState<ScheduleResponse | null>(null);
  const [timelineType, setTimelineType] = useState<EventType | "ALL">("ALL");
  const [timeline, setTimeline] = useState<ScheduledEvent[]>([]);
  const [difficulty, setDifficulty] = useState<DifficultyLevel>("LEVEL_II");
  const [draft, setDraft] = useState<ScenarioDraftResponse | null>(null);
  const [draftId, setDraftId] = useState("");
  const [eventForm, setEventForm] = useState<EventFormState>(initialEventForm);

  const crewPopulation = useMemo(
    () => missionPlan?.crew.reduce((sum, profile) => sum + profile.population, 0) ?? 0,
    [missionPlan],
  );

  const scheduleEvents = useMemo(() => {
    if (timeline.length > 0) {
      return sortEvents(timeline);
    }
    return sortEvents(schedule?.events ?? []);
  }, [schedule?.events, timeline]);

  const run = async (key: string, action: () => Promise<void>) => {
    setBusy(key);
    setError(null);
    setNotice(null);
    try {
      await action();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : "Unexpected frontend error");
    } finally {
      setBusy(null);
    }
  };

  const handleLogin = (event: FormEvent) => {
    event.preventDefault();
    void run("login", async () => {
      const response = await api.login(login, password);
      setSessionToken(response.message);
      setNotice("Session token accepted by backend.");
    });
  };

  const handleVerify = () =>
    run("verify", async () => {
      const response = await api.verify(sessionToken);
      setNotice(`Session verification: ${response.message}`);
    });

  const handleLogout = () =>
    run("logout", async () => {
      const response = await api.logout(sessionToken);
      setSessionToken("");
      setNotice(response.message);
    });

  const loadDefaultPlan = () =>
    run("default-plan", async () => {
      const [plan, count, resources] = await Promise.all([
        api.getDefaultMissionPlan(),
        api.getPlansCount(),
        api.getResourceTypes(),
      ]);
      setMissionPlan(plan);
      setPlansCount(count.message);
      setResourceTypes(resources);
      setDurationSols(plan.missionDurationSols || durationSols);
      setNotice("Default mission plan loaded.");
    });

  const loadMissionPlan = () =>
    run("mission-plan", async () => {
      const plan = await api.getMissionPlan(missionId);
      setMissionPlan(plan);
      setScheduleMissionId(missionId);
      setDurationSols(plan.missionDurationSols || durationSols);
      setNotice(`Mission plan ${missionId} loaded.`);
    });

  const loadCatalog = () =>
    run("catalog", async () => {
      const [nextCatalog, states] = await Promise.all([api.getModuleCatalog(), api.getModuleStates()]);
      setCatalog(nextCatalog);
      setNotice(`Catalog loaded. Module states: ${states.map(label).join(", ")}`);
    });

  const createSchedule = (event: FormEvent) => {
    event.preventDefault();
    void run("create-schedule", async () => {
      const nextSchedule = await api.createSchedule({
        missionPlanId: scheduleMissionId,
        durationSols,
      });
      setSchedule(nextSchedule);
      setScheduleId(nextSchedule.id);
      setTimeline([]);
      setNotice(`Schedule ${nextSchedule.id} created.`);
    });
  };

  const loadSchedule = () =>
    run("schedule", async () => {
      const nextSchedule = await api.getSchedule(scheduleId);
      setSchedule(nextSchedule);
      setTimeline([]);
      setNotice(`Schedule ${nextSchedule.id} loaded.`);
    });

  const loadTimeline = () =>
    run("timeline", async () => {
      const response = await api.getTimeline(
        scheduleId,
        timelineType === "ALL" ? undefined : timelineType,
      );
      setTimeline(response.eventsSortedBySol);
      setNotice(`${response.eventsSortedBySol.length} timeline events loaded.`);
    });

  const generateScenario = () =>
    run("scenario", async () => {
      const response = await api.generateScenario({
        missionPlanId: scheduleMissionId,
        durationSols,
        difficulty,
      });
      setDraft(response);
      setDraftId(response.id);
      setNotice(`Scenario draft ${response.id} generated.`);
    });

  const loadScenarioDraft = () =>
    run("draft", async () => {
      const response = await api.getScenarioDraft(draftId);
      setDraft(response);
      setNotice(`Scenario draft ${response.id} loaded.`);
    });

  const approveDraft = () =>
    run("approve-draft", async () => {
      const response = scheduleId
        ? await api.approveScenarioIntoSchedule(scheduleId, draftId || draft?.id || "")
        : await api.approveScenarioDraft(draftId || draft?.id || "");
      setSchedule(response);
      setScheduleId(response.id);
      setTimeline([]);
      setNotice(`Scenario approved into schedule ${response.id}.`);
    });

  const buildEventPayload = (): ScheduledEvent => {
    const base = {
      id: eventForm.id,
      type: eventForm.type,
      sol: eventForm.sol,
      description: eventForm.description,
    };

    if (eventForm.type === "THREAT") {
      return {
        ...base,
        threatType: eventForm.threatType,
        affectedElement: eventForm.affectedElement,
        impactValue: eventForm.impactValue,
        durationSols: eventForm.durationSols,
        impactUnit: eventForm.impactUnit,
      };
    }

    if (eventForm.type === "SUPPLY_DELIVERY") {
      return {
        ...base,
        content: {
          totalWeight: eventForm.totalWeight,
          items: [
            {
              itemId: eventForm.itemId,
              itemType: eventForm.itemType,
              quantity: eventForm.quantity,
              weight: eventForm.weight,
            },
          ],
        },
      };
    }

    return {
      ...base,
      moduleId: eventForm.moduleId,
      newState: eventForm.newState,
    };
  };

  const addEvent = (event: FormEvent) => {
    event.preventDefault();
    void run("add-event", async () => {
      const response = await api.addEvent(scheduleId, buildEventPayload());
      setSchedule(response);
      setTimeline([]);
      setEventForm((current) => ({ ...current, id: `${current.id}-next` }));
      setNotice(`Event added to schedule ${response.id}.`);
    });
  };

  const renderStatus = () => (
    <section className="status-strip" aria-live="polite">
      <span className={busy ? "status-dot busy" : "status-dot"}></span>
      <span>{busy ? `Working: ${label(busy)}` : "Ready"}</span>
      {notice && <strong>{notice}</strong>}
      {error && <strong className="error-text">{error}</strong>}
    </section>
  );

  return (
    <main className="app-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">Mars Mission Planner</p>
          <h1>Mission operations client</h1>
        </div>
        <div className="session-pill">
          <span>{sessionToken ? "Authenticated" : "No session"}</span>
          {scheduleId && <span>Schedule {scheduleId}</span>}
        </div>
      </header>

      <nav className="tabs" aria-label="Application sections">
        {(["session", "mission", "catalog", "schedule"] as const).map((item) => (
          <button
            className={view === item ? "active" : ""}
            key={item}
            onClick={() => setView(item)}
            type="button"
          >
            {label(item)}
          </button>
        ))}
      </nav>

      {renderStatus()}

      {view === "session" && (
        <section className="workspace two-column">
          <form className="panel" onSubmit={handleLogin}>
            <div className="panel-heading">
              <h2>Authentication</h2>
              <span>POST /api/auth/login</span>
            </div>
            <label>
              Login
              <input value={login} onChange={(event) => setLogin(event.target.value)} />
            </label>
            <label>
              Password
              <input
                type="password"
                value={password}
                onChange={(event) => setPassword(event.target.value)}
              />
            </label>
            <button disabled={busy === "login"} type="submit">
              Sign in
            </button>
          </form>

          <section className="panel">
            <div className="panel-heading">
              <h2>Session token</h2>
              <span>verify/logout</span>
            </div>
            <pre className="token-box">{sessionToken || "Token will appear after login."}</pre>
            <div className="button-row">
              <button disabled={!sessionToken || busy === "verify"} onClick={handleVerify} type="button">
                Verify
              </button>
              <button disabled={!sessionToken || busy === "logout"} onClick={handleLogout} type="button">
                Logout
              </button>
            </div>
          </section>
        </section>
      )}

      {view === "mission" && (
        <section className="workspace">
          <section className="panel">
            <div className="panel-heading">
              <h2>Mission configuration</h2>
              <span>GET /api/conf</span>
            </div>
            <div className="toolbar">
              <label>
                Mission id
                <input value={missionId} onChange={(event) => setMissionId(event.target.value)} />
              </label>
              <button onClick={loadDefaultPlan} type="button">
                Load default
              </button>
              <button onClick={loadMissionPlan} type="button">
                Load by id
              </button>
            </div>
          </section>

          {missionPlan ? (
            <section className="dashboard-grid">
              <article className="metric">
                <span>Duration</span>
                <strong>{missionPlan.missionDurationSols} sols</strong>
              </article>
              <article className="metric">
                <span>Crew population</span>
                <strong>{crewPopulation}</strong>
              </article>
              <article className="metric">
                <span>Modules</span>
                <strong>{missionPlan.modules.length}</strong>
              </article>
              <article className="metric">
                <span>Plans in store</span>
                <strong>{plansCount ?? "unknown"}</strong>
              </article>
              <section className="panel wide">
                <div className="panel-heading">
                  <h2>Starting resources</h2>
                  <span>{resourceTypes.length ? resourceTypes.join(", ") : "known resources"}</span>
                </div>
                <div className="table">
                  {missionPlan.startingResources.map((resource) => (
                    <div className="table-row" key={resource.resourceType}>
                      <span>{label(resource.resourceType)}</span>
                      <strong>{resource.quantity}</strong>
                    </div>
                  ))}
                </div>
              </section>
              <section className="panel wide">
                <div className="panel-heading">
                  <h2>Assigned modules</h2>
                  <span>Max start weight {missionPlan.maxStartingWeight}</span>
                </div>
                <div className="list">
                  {missionPlan.modules.map((module) => (
                    <article className="list-item" key={`${module.name}-${module.weight}`}>
                      <strong>{module.name}</strong>
                      <span>{module.type?.name ?? "unknown type"}</span>
                      <span>{label(module.status)} | {module.weight} kg</span>
                    </article>
                  ))}
                </div>
              </section>
            </section>
          ) : (
            <EmptyState text="Load a mission plan to inspect crew, resources and modules." />
          )}
        </section>
      )}

      {view === "catalog" && (
        <section className="workspace">
          <section className="panel">
            <div className="panel-heading">
              <h2>Module catalog</h2>
              <span>GET /api/conf/module-catalog</span>
            </div>
            <button onClick={loadCatalog} type="button">
              Refresh catalog
            </button>
          </section>

          {catalog ? (
            <section className="two-column">
              <section className="panel">
                <div className="panel-heading">
                  <h2>Modules</h2>
                  <span>{catalog.moduleList.length} entries</span>
                </div>
                <div className="list">
                  {catalog.moduleList.map((module) => (
                    <article className="list-item" key={module.name}>
                      <strong>{module.name}</strong>
                      <span>{module.type?.name ?? "unknown type"}</span>
                      <span>{label(module.status)} | {module.weight} kg</span>
                    </article>
                  ))}
                </div>
              </section>
              <section className="panel">
                <div className="panel-heading">
                  <h2>Module types</h2>
                  <span>{catalog.typeList.length} entries</span>
                </div>
                <div className="list">
                  {catalog.typeList.map((type) => (
                    <article className="list-item" key={type.name}>
                      <strong>{type.name}</strong>
                      <span>Consumes {type.resourceConsumption?.length ?? 0} resources</span>
                      <span>Produces {type.resourceProduction?.length ?? 0} resources</span>
                    </article>
                  ))}
                </div>
              </section>
            </section>
          ) : (
            <EmptyState text="Refresh the catalog to see modules and module types from backend." />
          )}
        </section>
      )}

      {view === "schedule" && (
        <section className="workspace">
          <section className="three-column">
            <form className="panel" onSubmit={createSchedule}>
              <div className="panel-heading">
                <h2>Create schedule</h2>
                <span>POST /api/schedule</span>
              </div>
              <label>
                Mission plan id
                <input
                  value={scheduleMissionId}
                  onChange={(event) => setScheduleMissionId(event.target.value)}
                />
              </label>
              <label>
                Duration sols
                <input
                  min={1}
                  type="number"
                  value={durationSols}
                  onChange={(event) => setDurationSols(Number(event.target.value))}
                />
              </label>
              <button type="submit">Create</button>
            </form>

            <section className="panel">
              <div className="panel-heading">
                <h2>Load timeline</h2>
                <span>GET /timeline</span>
              </div>
              <label>
                Schedule id
                <input value={scheduleId} onChange={(event) => setScheduleId(event.target.value)} />
              </label>
              <label>
                Event filter
                <select
                  value={timelineType}
                  onChange={(event) => setTimelineType(event.target.value as EventType | "ALL")}
                >
                  <option value="ALL">All</option>
                  {eventTypes.map((type) => (
                    <option key={type} value={type}>
                      {label(type)}
                    </option>
                  ))}
                </select>
              </label>
              <div className="button-row">
                <button disabled={!scheduleId} onClick={loadSchedule} type="button">
                  Schedule
                </button>
                <button disabled={!scheduleId} onClick={loadTimeline} type="button">
                  Timeline
                </button>
              </div>
            </section>

            <section className="panel">
              <div className="panel-heading">
                <h2>Scenario</h2>
                <span>generate/approve</span>
              </div>
              <label>
                Difficulty
                <select
                  value={difficulty}
                  onChange={(event) => setDifficulty(event.target.value as DifficultyLevel)}
                >
                  {difficultyLevels.map((level) => (
                    <option key={level} value={level}>
                      {label(level)}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Draft id
                <input value={draftId} onChange={(event) => setDraftId(event.target.value)} />
              </label>
              <div className="button-row">
                <button onClick={generateScenario} type="button">
                  Generate
                </button>
                <button disabled={!draftId && !draft} onClick={loadScenarioDraft} type="button">
                  Load
                </button>
                <button disabled={!draftId && !draft} onClick={approveDraft} type="button">
                  Approve
                </button>
              </div>
            </section>
          </section>

          <section className="two-column">
            <form className="panel" onSubmit={addEvent}>
              <div className="panel-heading">
                <h2>Event editor</h2>
                <span>POST /events</span>
              </div>
              <label>
                Event id
                <input
                  value={eventForm.id}
                  onChange={(event) => setEventForm({ ...eventForm, id: event.target.value })}
                />
              </label>
              <label>
                Type
                <select
                  value={eventForm.type}
                  onChange={(event) =>
                    setEventForm({ ...eventForm, type: event.target.value as EventType })
                  }
                >
                  {eventTypes.map((type) => (
                    <option key={type} value={type}>
                      {label(type)}
                    </option>
                  ))}
                </select>
              </label>
              <div className="split-fields">
                <label>
                  Sol
                  <input
                    min={1}
                    type="number"
                    value={eventForm.sol}
                    onChange={(event) => setEventForm({ ...eventForm, sol: Number(event.target.value) })}
                  />
                </label>
                <label>
                  Description
                  <input
                    value={eventForm.description}
                    onChange={(event) =>
                      setEventForm({ ...eventForm, description: event.target.value })
                    }
                  />
                </label>
              </div>

              {eventForm.type === "THREAT" && (
                <div className="event-fields">
                  <label>
                    Threat type
                    <select
                      value={eventForm.threatType}
                      onChange={(event) =>
                        setEventForm({ ...eventForm, threatType: event.target.value as ThreatType })
                      }
                    >
                      {threatTypes.map((type) => (
                        <option key={type} value={type}>
                          {label(type)}
                        </option>
                      ))}
                    </select>
                  </label>
                  <div className="split-fields">
                    <label>
                      Affected
                      <input
                        value={eventForm.affectedElement}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, affectedElement: event.target.value })
                        }
                      />
                    </label>
                    <label>
                      Impact unit
                      <input
                        value={eventForm.impactUnit}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, impactUnit: event.target.value })
                        }
                      />
                    </label>
                  </div>
                  <div className="split-fields">
                    <label>
                      Impact
                      <input
                        min={0}
                        type="number"
                        value={eventForm.impactValue}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, impactValue: Number(event.target.value) })
                        }
                      />
                    </label>
                    <label>
                      Duration
                      <input
                        min={1}
                        type="number"
                        value={eventForm.durationSols}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, durationSols: Number(event.target.value) })
                        }
                      />
                    </label>
                  </div>
                </div>
              )}

              {eventForm.type === "SUPPLY_DELIVERY" && (
                <div className="event-fields">
                  <div className="split-fields">
                    <label>
                      Item id
                      <input
                        value={eventForm.itemId}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, itemId: event.target.value })
                        }
                      />
                    </label>
                    <label>
                      Item type
                      <select
                        value={eventForm.itemType}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, itemType: event.target.value as DeliveryItemType })
                        }
                      >
                        {deliveryItemTypes.map((type) => (
                          <option key={type} value={type}>
                            {label(type)}
                          </option>
                        ))}
                      </select>
                    </label>
                  </div>
                  <div className="split-fields">
                    <label>
                      Quantity
                      <input
                        min={0}
                        type="number"
                        value={eventForm.quantity}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, quantity: Number(event.target.value) })
                        }
                      />
                    </label>
                    <label>
                      Weight
                      <input
                        min={0}
                        type="number"
                        value={eventForm.weight}
                        onChange={(event) =>
                          setEventForm({ ...eventForm, weight: Number(event.target.value) })
                        }
                      />
                    </label>
                  </div>
                  <label>
                    Total weight
                    <input
                      min={0}
                      type="number"
                      value={eventForm.totalWeight}
                      onChange={(event) =>
                        setEventForm({ ...eventForm, totalWeight: Number(event.target.value) })
                      }
                    />
                  </label>
                </div>
              )}

              {eventForm.type === "MODULE_STATE_CHANGE" && (
                <div className="event-fields">
                  <label>
                    Module id
                    <input
                      value={eventForm.moduleId}
                      onChange={(event) =>
                        setEventForm({ ...eventForm, moduleId: event.target.value })
                      }
                    />
                  </label>
                  <label>
                    New state
                    <select
                      value={eventForm.newState}
                      onChange={(event) =>
                        setEventForm({ ...eventForm, newState: event.target.value as ModuleState })
                      }
                    >
                      {moduleStates.map((state) => (
                        <option key={state} value={state}>
                          {label(state)}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>
              )}

              <button disabled={!scheduleId} type="submit">
                Add event
              </button>
            </form>

            <section className="panel">
              <div className="panel-heading">
                <h2>Schedule events</h2>
                <span>{scheduleEvents.length} visible</span>
              </div>
              {scheduleEvents.length ? (
                <div className="list timeline-list">
                  {scheduleEvents.map((event) => (
                    <article className="list-item" key={event.id}>
                      <strong>Sol {event.sol}: {label(event.type)}</strong>
                      <span>{event.description}</span>
                      <span>{eventSummary(event)}</span>
                    </article>
                  ))}
                </div>
              ) : (
                <EmptyState text="Create or load a schedule to inspect its timeline." />
              )}
            </section>
          </section>

          {draft && (
            <section className="panel">
              <div className="panel-heading">
                <h2>Scenario draft {draft.id}</h2>
                <span>{label(draft.difficulty)} | {draft.proposedEvents.length} proposed events</span>
              </div>
              <div className="list compact-list">
                {sortEvents(draft.proposedEvents).map((event) => (
                  <article className="list-item" key={event.id}>
                    <strong>Sol {event.sol}: {label(event.type)}</strong>
                    <span>{event.description}</span>
                  </article>
                ))}
              </div>
            </section>
          )}
        </section>
      )}
    </main>
  );
}

function EmptyState({ text }: { text: string }) {
  return <section className="empty-state">{text}</section>;
}

export default App;
