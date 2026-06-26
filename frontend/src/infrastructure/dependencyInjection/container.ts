import { ApiAuthAdapter } from "../adapters/ApiAuthAdapter";
import { ApiScheduleAdapter } from "../adapters/ApiScheduleAdapter";
import { ApiMissionAdapter } from "../adapters/ApiMissionAdapter";
import { ApiSimulationAdapter } from "../adapters/ApiSimulationAdapter";

import type { IAuthRepository } from "../../core/application/ports/IAuthRepository";
import type { IMissionRepository } from "../../core/application/ports/IMissionRepository";
import type { IScheduleRepository } from "../../core/application/ports/IScheduleRepository";
import type { ISimulationRepository } from "../../core/application/ports/ISimulationRepository";

export const authRepository: IAuthRepository = new ApiAuthAdapter();
export const missionRepository: IMissionRepository = new ApiMissionAdapter();
export const scheduleRepository: IScheduleRepository = new ApiScheduleAdapter();
export const simulationRepository: ISimulationRepository = new ApiSimulationAdapter();
