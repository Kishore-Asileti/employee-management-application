#!/usr/bin/env bash
# ================================================================
# start-frontend.sh  — Install deps and start Angular dev server
# ================================================================
set -e
cd "$(dirname "$0")/frontend"

echo "========================================"
echo "  Installing Angular dependencies"
echo "========================================"
npm install

echo ""
echo "========================================"
echo "  Starting Angular on http://localhost:4200"
echo "  Proxying /api, /camunda, /engine-rest → localhost:8081"
echo "========================================"
ng serve --proxy-config proxy.conf.json --open
