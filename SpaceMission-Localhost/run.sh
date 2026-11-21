#!/usr/bin/env bash
# One-command launcher for macOS / Linux:   ./run.sh
cd "$(dirname "$0")" || exit 1

if ! command -v javac >/dev/null 2>&1; then
  echo "Java JDK 17 or newer was not found. Install a JDK (e.g. from https://adoptium.net) and run this again."
  exit 1
fi

JV=$(javac -version 2>&1 | grep '^javac' | grep -oE '[0-9]+' | head -1)
if [ -n "$JV" ] && [ "$JV" -lt 17 ]; then
  echo "Java $JV found, but Java 17 or newer is required (the bundled database needs it)."
  exit 1
fi

mkdir -p out
echo "Compiling..."
javac -cp "lib/*" -d out *.java || { echo "Compilation failed."; exit 1; }

PORT="${PORT:-8080}"
# Best effort: open the browser a few seconds after the server starts.
( sleep 3
  if command -v open >/dev/null 2>&1; then open "http://localhost:$PORT"
  elif command -v xdg-open >/dev/null 2>&1; then xdg-open "http://localhost:$PORT" >/dev/null 2>&1
  fi ) &

PORT="$PORT" java -cp "out:lib/*" MissionWebApp
