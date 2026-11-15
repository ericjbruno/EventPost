#!/bin/sh
# Renders each diagram's SVG to a PNG at 2x, using headless Chrome.
# Usage: ./render.sh [fig*.svg ...]   (default: every fig*.svg here)
CHROME="/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
cd "$(dirname "$0")"
[ $# -eq 0 ] && set -- fig*.svg
for svg in "$@"; do
    w=$(sed -n 's/.*<svg[^>]* width="\([0-9]*\)".*/\1/p' "$svg" | head -1)
    h=$(sed -n 's/.*<svg[^>]* height="\([0-9]*\)".*/\1/p' "$svg" | head -1)
    "$CHROME" --headless --disable-gpu --hide-scrollbars --force-device-scale-factor=2 \
        --window-size="$w,$h" --screenshot="$PWD/${svg%.svg}.png" "file://$PWD/$svg" 2>/dev/null
done
