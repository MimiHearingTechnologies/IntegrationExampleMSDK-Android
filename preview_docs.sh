#!/bin/bash

# Fail fast
set -e
set -o pipefail

mkdocs build

cd site

PORT="1313"
echo "==========================================="
echo "Open http://localhost:$PORT/ in your browser"
echo "==========================================="
python3 -m http.server $PORT