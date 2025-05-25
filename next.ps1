Invoke-RestMethod `
  -Uri "http://localhost:8080/game/next" `
  -Method Post `
  -ContentType "application/json" `
  -Body (ConvertTo-Json @{
      boardName = "BLINKER"
      steps = 10
    } -Depth 5) `
| ConvertTo-Json -Depth 5 -Compress
