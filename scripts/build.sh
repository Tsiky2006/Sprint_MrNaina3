#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
mkdir -p "$ROOT_DIR/build/classes"

# compile sources (adjust servlet jar path if needed)
javac -cp "$ROOT_DIR/lib/servlet-api.jar" -d "$ROOT_DIR/build/classes" "$ROOT_DIR/src/com/sprint0"/*.java

# create WAR-like archive (simple)
rm -f "$ROOT_DIR/build/Sprint_MrNaina.war"
jar cvf "$ROOT_DIR/build/Sprint_MrNaina.war" -C "$ROOT_DIR/WebContent" . -C "$ROOT_DIR/build/classes" .

echo "Build terminé : build/Sprint_MrNaina.war"