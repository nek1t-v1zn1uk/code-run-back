#!/bin/bash

echo "Stopping Docker containers..."
if [ -d "deploy" ]; then
    cd deploy
    docker compose down
    cd ..
else
    echo "deploy/ directory not found. Skipping Docker teardown."
fi

echo "Stopping Isolate Daemon..."
if systemctl is-active --quiet code-run-isolate-daemon.service; then
    sudo systemctl stop code-run-isolate-daemon.service
    echo "Isolate Daemon stopped."
else
    echo "Isolate Daemon is not running."
fi

echo "Project stopped successfully!"
