Invoke-RestMethod `
  -Uri "http://localhost:8080/game/next" `
  -Method Post `
  -ContentType "application/json" `
  -Body (ConvertTo-Json @{
      board = ((0, 0), (0, 0))
      boardName = 'BLINKER'
      steps = -1
    } -Depth 5) `
| ConvertTo-Json -Depth 5 -Compress
