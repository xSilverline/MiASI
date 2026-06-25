param(
  [string] $ApiBaseUrl = "http://localhost:8080",
  [switch] $ClearCatalog,
  [switch] $ClearTimeline
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

  $json = $Body | ConvertTo-Json -Depth 20
  return Invoke-RestMethod -Method $Method -Uri $uri -ContentType "application/json" -Body $json
}

function As-Array {
  param([object] $Value)

  if ($null -eq $Value) {
    return @()
  }

  return @($Value)
}

if ($ClearTimeline) {
  Write-Host "Clearing timeline..."
  $timeline = As-Array (Invoke-Api -Method "GET" -Path "/api/timeline")
  foreach ($sol in $timeline) {
    foreach ($event in As-Array $sol.events) {
      Invoke-Api -Method "DELETE" -Path "/api/timeline/events/$($event.id)" | Out-Null
    }
  }
}

if ($ClearCatalog) {
  Write-Host "Clearing event catalog..."
  $catalog = As-Array (Invoke-Api -Method "GET" -Path "/api/event-catalog")
  foreach ($event in $catalog) {
    Invoke-Api -Method "DELETE" -Path "/api/event-catalog/$($event.id)" | Out-Null
  }
}

$eventDefinitions = @(
  @{
    name = "Food delivery"
    type = "SUPPLY_DELIVERY"
    description = "Cargo lander delivers food supplies."
    affectedElement = "warehouse"
    consequence = "Food stock increased."
    effects = @(
      @{
        target = "FOOD"
        value = 30
        unit = "KG"
        description = "Extra food supply."
      }
    )
  },
  @{
    name = "Oxygen delivery"
    type = "SUPPLY_DELIVERY"
    description = "Cargo lander delivers oxygen tanks."
    affectedElement = "life-support"
    consequence = "Oxygen reserves increased."
    effects = @(
      @{
        target = "OXYGEN"
        value = 80
        unit = "L"
        description = "Extra oxygen supply."
      }
    )
  },
  @{
    name = "Dust storm"
    type = "THREAT"
    description = "Dust storm hits solar panels."
    affectedElement = "solar-panels"
    consequence = "Energy production reduced."
    effects = @(
      @{
        target = "ENERGY"
        value = -15
        unit = "PERCENT"
        description = "Lower energy production."
      }
    )
  },
  @{
    name = "Water leak"
    type = "THREAT"
    description = "Water storage leak detected."
    affectedElement = "water-storage"
    consequence = "Water stock reduced."
    effects = @(
      @{
        target = "WATER"
        value = -20
        unit = "L"
        description = "Lost water."
      }
    )
  },
  @{
    name = "Habitat pressure drop"
    type = "MODULE_STATE_CHANGE"
    description = "Habitat pressure is unstable."
    affectedElement = "habitat"
    consequence = "Habitat module requires inspection."
    effects = @(
      @{
        target = "habitat"
        value = -1
        unit = "STATE"
        description = "Module state changed."
      }
    )
  }
)

Write-Host "Adding event definitions to catalog..."
$createdCatalogEvents = As-Array (Invoke-Api `
  -Method "POST" `
  -Path "/api/event-catalog/batch" `
  -Body $eventDefinitions)

$eventsByName = @{}
foreach ($event in $createdCatalogEvents) {
  $eventsByName[$event.name] = $event
}

$timelineEvents = @(
  @{
    sol = 5
    eventDefinitionId = $eventsByName["Oxygen delivery"].id
  },
  @{
    sol = 10
    eventDefinitionId = $eventsByName["Food delivery"].id
  },
  @{
    sol = 15
    eventDefinitionId = $eventsByName["Dust storm"].id
  },
  @{
    sol = 18
    eventDefinitionId = $eventsByName["Water leak"].id
  },
  @{
    sol = 22
    eventDefinitionId = $eventsByName["Habitat pressure drop"].id
  }
)

Write-Host "Adding catalog events to timeline..."
$createdTimelineEvents = As-Array (Invoke-Api `
  -Method "POST" `
  -Path "/api/timeline/events/batch" `
  -Body $timelineEvents)

$timeline = As-Array (Invoke-Api -Method "GET" -Path "/api/timeline")
$deliveries = As-Array (Invoke-Api -Method "GET" -Path "/api/timeline/deliveries")
$threats = As-Array (Invoke-Api -Method "GET" -Path "/api/timeline/threats")

Write-Host ""
Write-Host "Seed complete."
Write-Host "Catalog definitions added: $($createdCatalogEvents.Count)"
Write-Host "Timeline events added: $($createdTimelineEvents.Count)"
Write-Host "Timeline sols returned: $($timeline.Count)"
Write-Host "Delivery sols returned: $($deliveries.Count)"
Write-Host "Threat sols returned: $($threats.Count)"
Write-Host ""
Write-Host "Useful URLs:"
Write-Host "$($ApiBaseUrl.TrimEnd('/'))/swagger-ui.html"
Write-Host "$($ApiBaseUrl.TrimEnd('/'))/api/event-catalog"
Write-Host "$($ApiBaseUrl.TrimEnd('/'))/api/timeline"
