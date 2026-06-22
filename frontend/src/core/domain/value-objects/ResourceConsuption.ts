export interface ConsumptionValues {
  opt: number;
  min: number;
}
export interface ResourceConsumption {
  maleFood: ConsumptionValues;
  femaleFood: ConsumptionValues;
  oxygen: ConsumptionValues;
  water: ConsumptionValues;
}
