param(
  [string] $ApiBaseUrl = "http://localhost:8080",
  [int] $OverrideMissionId = -1,
  [switch] $ModulesOnly,
  [switch] $PlanOnly
)

$ErrorActionPreference = "Stop"

function Invoke-Api {
  param(
    [string] $Method,
    [string] $Path,
    [object] $Body = $null
  )

  $uri = "$($ApiBaseUrl.TrimEnd('/'))$Path"
  if ($null -eq $Body) {
    return Invoke-RestMethod -Method $Method -Uri $uri
  }

  $json = $Body | ConvertTo-Json -Depth 30
  try {
    return Invoke-RestMethod -Method $Method -Uri $uri -ContentType "application/json" -Body $json
  } catch {
    Write-Host ""
    Write-Host "Request failed: $Method $uri"
    Write-Host "Payload:"
    Write-Host $json
    throw
  }
}

$modules = @(
  @{
    name = "Solar Array"
    status = "ACTIVE"
    category = "ENERGY_MODULE"
    weight = 1200
    resourceConsumption = @()
    resourceProduction = @(
      @{ resourceType = "ENERGY"; quantity = 45 }
    )
  },
  @{
    name = "Battery Bank"
    status = "ACTIVE"
    category = "ENERGY_MODULE"
    weight = 900
    resourceConsumption = @()
    resourceProduction = @(
      @{ resourceType = "ENERGY"; quantity = 8 }
    )
  },
  @{
    name = "Greenhouse"
    status = "ACTIVE"
    category = "UTILITY_MODULE"
    weight = 1800
    resourceConsumption = @(
      @{ resourceType = "ENERGY"; quantity = 10 },
      @{ resourceType = "WATER"; quantity = 6 }
    )
    resourceProduction = @(
      @{ resourceType = "FOOD"; quantity = 18 },
      @{ resourceType = "OXYGEN"; quantity = 5 }
    )
  },
  @{
    name = "Oxygen Generator"
    status = "ACTIVE"
    category = "UTILITY_MODULE"
    weight = 1100
    resourceConsumption = @(
      @{ resourceType = "ENERGY"; quantity = 12 },
      @{ resourceType = "WATER"; quantity = 2 }
    )
    resourceProduction = @(
      @{ resourceType = "OXYGEN"; quantity = 35 }
    )
  },
  @{
    name = "Water Recycler"
    status = "ACTIVE"
    category = "UTILITY_MODULE"
    weight = 1300
    resourceConsumption = @(
      @{ resourceType = "ENERGY"; quantity = 8 }
    )
    resourceProduction = @(
      @{ resourceType = "WATER"; quantity = 24 }
    )
  },
  @{
    name = "Habitat Module"
    status = "ACTIVE"
    category = "UTILITY_MODULE"
    weight = 2600
    resourceConsumption = @(
      @{ resourceType = "ENERGY"; quantity = 15 },
      @{ resourceType = "OXYGEN"; quantity = 4 }
    )
    resourceProduction = @()
  },
  @{
    name = "Research Lab"
    status = "ACTIVE"
    category = "UTILITY_MODULE"
    weight = 1600
    resourceConsumption = @(
      @{ resourceType = "ENERGY"; quantity = 14 },
      @{ resourceType = "WATER"; quantity = 1 }
    )
    resourceProduction = @()
  }
)

if (-not $PlanOnly) {
  Write-Host "Adding/updating module catalog..."
  foreach ($module in $modules) {
    $response = Invoke-Api -Method "POST" -Path "/api/conf/module" -Body $module
    Write-Host "  module=$($module.name), index=$($response.message)"
  }
}

if ($ModulesOnly) {
  Write-Host ""
  Write-Host "Modules seed complete."
  Write-Host "$($ApiBaseUrl.TrimEnd('/'))/api/conf/module-catalog"
  exit 0
}

$missionPlan = @{
  crew = @(
    @{
      name = "Engineering Crew"
      population = 6
      optimalDemand = @{
        FOOD = 1.0
        OXYGEN = 1.1
        WATER = 1.8
      }
      minimalDemand = @{
        FOOD = 0.55
        OXYGEN = 0.75
        WATER = 0.9
      }
    },
    @{
      name = "Science Crew"
      population = 4
      optimalDemand = @{
        FOOD = 0.9
        OXYGEN = 1.0
        WATER = 1.6
      }
      minimalDemand = @{
        FOOD = 0.5
        OXYGEN = 0.7
        WATER = 0.85
      }
    }
  )
  missionDurationSols = 45
  startingResources = @(
    @{ resourceType = "FOOD"; quantity = 520 },
    @{ resourceType = "OXYGEN"; quantity = 760 },
    @{ resourceType = "WATER"; quantity = 680 },
    @{ resourceType = "ENERGY"; quantity = 420 }
  )
  modules = @(
    $modules[0],
    $modules[2],
    $modules[3],
    $modules[4],
    $modules[5]
  )
  maxStartingWeight = 12500
}

Write-Host "Adding mission plan with starting supplies..."
if ($OverrideMissionId -ge 0) {
  $result = Invoke-Api `
    -Method "POST" `
    -Path "/api/conf/plan?override=$OverrideMissionId" `
    -Body $missionPlan
} else {
  $result = Invoke-Api -Method "POST" -Path "/api/conf/plan" -Body $missionPlan
}

$missionId = $result.message

Write-Host ""
Write-Host "Mission seed complete."
Write-Host "Mission id: $missionId"
Write-Host "Modules in catalog payload: $($modules.Count)"
Write-Host "Starting supplies:"
foreach ($resource in $missionPlan.startingResources) {
  Write-Host "  $($resource.resourceType): $($resource.quantity)"
}
Write-Host ""
Write-Host "Useful URLs:"
Write-Host "$($ApiBaseUrl.TrimEnd('/'))/api/conf/module-catalog"
Write-Host "$($ApiBaseUrl.TrimEnd('/'))/api/conf/$missionId/plan"
