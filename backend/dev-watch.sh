#!/bin/sh
set -eu

hash_sources() {
  find src/main/java src/main/resources -type f \
    \( -name "*.java" -o -name "*.yml" -o -name "*.yaml" -o -name "*.properties" \) \
    -exec sha1sum {} + 2>/dev/null | sort | sha1sum | cut -d' ' -f1
}

last_hash=""

echo "[dev-watch] Source watcher ativo (polling 2s)."
while true; do
  new_hash="$(hash_sources)"

  if [ "$new_hash" != "$last_hash" ]; then
    if [ -n "$last_hash" ]; then
      echo "[dev-watch] Mudancas detectadas, recompilando..."
    fi

    mvn -q -DskipTests compile || true
    last_hash="$new_hash"
  fi

  sleep 2
done
