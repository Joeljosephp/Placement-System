#!/usr/bin/env bash
set -e

mkdir -p bin
javac -encoding UTF-8 -d bin $(find src test -name "*.java")

echo "Running Unit and Integration Test Suite..."
java -cp bin test.PlacementSystemTest "$@"

echo ""
echo "Running End-to-End Scenario Test Suite..."
java -cp bin test.ScenarioReviewTest "$@"
