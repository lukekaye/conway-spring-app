#!/bin/sh
curl -s -X POST "http://localhost:8080/game/next" \
  -H "Content-Type: application/json" \
  -d '{"board":[[0,0],[0,0]],"boardName":"BLINKER","steps":-1}' \
| jq "."
