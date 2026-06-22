
import { ApiAuthAdapter } from "../adapters/ApiAuthAdapter";
import { MockScheduleAdapter } from "../adapters/ScheduleAdapter";
import { ApiMissionAdapter } from "../adapters/ApiMissionAdapter";

import type { IAuthRepository } from "../../core/application/ports/IAuthRepository";
import type { IMissionRepository } from "../../core/application/ports/IMissionRepository";
import type { IScheduleRepository } from "../../core/application/ports/IScheduleRepository";

export const authRepository: IAuthRepository = new ApiAuthAdapter();
export const missionRepository: IMissionRepository = new ApiMissionAdapter();
export const scheduleRepository: IScheduleRepository =
  new MockScheduleAdapter();
