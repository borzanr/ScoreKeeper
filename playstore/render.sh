#!/usr/bin/env bash
# Regera os assets da Play Store a partir dos HTMLs em src/.
# Uso: bash playstore/render.sh
set -euo pipefail

CHROME="${CHROME:-/c/Program Files (x86)/Google/Chrome/Application/chrome.exe}"
[ -f "$CHROME" ] || CHROME="/c/Program Files/Google/Chrome/Application/chrome.exe"
[ -f "$CHROME" ] || { echo "Chrome não encontrado; defina CHROME=caminho"; exit 1; }

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# O Chrome no Windows precisa de file:// com caminho absoluto no formato dele.
WIN_DIR="$(cd "$HERE" && pwd -W 2>/dev/null || echo "$HERE")"
URL="file:///${WIN_DIR}/src"
OUT="$(echo "$WIN_DIR" | tr '/' '\\')"

shot() { # shot <url> <arquivo-saida> <largura> <altura>
  "$CHROME" --headless=new --disable-gpu --hide-scrollbars \
    --force-device-scale-factor=1 --virtual-time-budget=3000 \
    --allow-file-access-from-files \
    --screenshot="$2" --window-size="$3,$4" "$1" > /dev/null 2>&1
  echo "gerado: $2"
}

# O ícone (icon-512.png) sai da arte do mago e já está pronto; o gráfico de
# destaque é por idioma da ficha.
shot "$URL/feature.html"         "${OUT}\\feature-graphic-1024x500.png"    1024 500
shot "$URL/feature.html?lang=en" "${OUT}\\feature-graphic-1024x500-en.png" 1024 500
shot "$URL/feature.html?lang=es" "${OUT}\\feature-graphic-1024x500-es.png" 1024 500

# Um conjunto de capturas por idioma; cada um só sai se as telas existirem.
render_set() { # render_set <lang> <pasta-origem> <pasta-saida>
  [ -f "$HERE/src/$2/scoreboard.png" ] || { echo "sem telas em src/$2, pulando $1"; return; }
  mkdir -p "$HERE/$3"
  for n in 1 2 3 4 5; do
    shot "$URL/shot.html?n=$n&lang=$1" "${OUT}\\$3\\0$n.png" 1080 1920
  done
}

render_set pt shots    screenshots
render_set en shots-en screenshots-en
render_set es shots-es screenshots-es
