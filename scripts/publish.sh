#!/usr/bin/env bash
# Skript pro sestavení a přípravu release buildu pro Google Play.
# Požadavky: Java, Android SDK, keytool, keystore.properties v kořeni projektu.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
OUTPUT_DIR="$PROJECT_ROOT/release-output"
GRADLEW="$PROJECT_ROOT/gradlew"

# ── Barvy ──────────────────────────────────────────────────────────────────────
GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC} $*"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $*"; }
error() { echo -e "${RED}[CHYBA]${NC} $*"; exit 1; }

# ── Kontrola prerekvizit ───────────────────────────────────────────────────────
info "Kontrola prerekvizit..."
command -v java      >/dev/null 2>&1 || error "Java není dostupná. Nainstaluj JDK."
command -v keytool   >/dev/null 2>&1 || error "keytool není dostupný (součást JDK)."
[ -f "$GRADLEW" ]    || error "gradlew nebyl nalezen v $PROJECT_ROOT"
[ -x "$GRADLEW" ]    || chmod +x "$GRADLEW"

KEYSTORE_PROPS="$PROJECT_ROOT/keystore.properties"
[ -f "$KEYSTORE_PROPS" ] || error "keystore.properties nebyl nalezen. Spusť nejdřív scripts/generate-keystore.sh"

# ── Načtení konfigurace keystoru ───────────────────────────────────────────────
STORE_FILE=$(grep '^storeFile' "$KEYSTORE_PROPS" | cut -d'=' -f2 | tr -d ' ')
KEY_ALIAS=$(grep  '^keyAlias'  "$KEYSTORE_PROPS" | cut -d'=' -f2 | tr -d ' ')
[ -f "$PROJECT_ROOT/$STORE_FILE" ] || \
  [ -f "$STORE_FILE" ] || \
  error "Keystore soubor '$STORE_FILE' nebyl nalezen."

info "Keystore: $STORE_FILE  |  Alias: $KEY_ALIAS"

# ── Příprava výstupního adresáře ───────────────────────────────────────────────
mkdir -p "$OUTPUT_DIR"

# ── Čištění starých buildů ─────────────────────────────────────────────────────
info "Čistím starý build..."
cd "$PROJECT_ROOT"
"$GRADLEW" clean --quiet

# ── Sestavení release AAB (doporučeno pro Google Play) ────────────────────────
info "Sestavuji release AAB (Android App Bundle)..."
"$GRADLEW" :app:bundleRelease

AAB_SRC="$PROJECT_ROOT/app/build/outputs/bundle/release/app-release.aab"
[ -f "$AAB_SRC" ] || error "AAB nebyl vygenerován. Zkontroluj konfiguraci signingConfig v build.gradle.kts"

AAB_DEST="$OUTPUT_DIR/primitive-device-stoic-release.aab"
cp "$AAB_SRC" "$AAB_DEST"
info "AAB zkopírováno: $AAB_DEST"

# ── Volitelně: sestavení APK (pro přímou distribuci / testování) ───────────────
read -rp "Sestavit také APK? (y/N): " BUILD_APK
if [[ "$BUILD_APK" =~ ^[Yy]$ ]]; then
  info "Sestavuji release APK..."
  "$GRADLEW" :app:assembleRelease

  APK_SRC="$PROJECT_ROOT/app/build/outputs/apk/release/app-release.apk"
  [ -f "$APK_SRC" ] || error "APK nebylo vygenerováno."
  APK_DEST="$OUTPUT_DIR/primitive-device-stoic-release.apk"
  cp "$APK_SRC" "$APK_DEST"
  info "APK zkopírováno: $APK_DEST"
fi

# ── Ověření podpisu AAB ────────────────────────────────────────────────────────
info "Ověřuji podpis AAB..."
if command -v bundletool >/dev/null 2>&1; then
  bundletool validate --bundle="$AAB_DEST" && info "AAB je validní." || warn "Validace bundletool selhala."
else
  warn "bundletool není nainstalován – přeskočení validace. (https://github.com/google/bundletool)"
fi

# ── Výpis SHA-1 / SHA-256 fingerprintu certifikátu ────────────────────────────
info "Otisk podpisového certifikátu:"
STORE_PASS=$(grep '^storePassword' "$KEYSTORE_PROPS" | cut -d'=' -f2 | tr -d ' ')
STORE_PATH="$PROJECT_ROOT/$STORE_FILE"
[ -f "$STORE_PATH" ] || STORE_PATH="$STORE_FILE"

keytool -list -v \
  -keystore "$STORE_PATH" \
  -alias "$KEY_ALIAS" \
  -storepass "$STORE_PASS" \
  2>/dev/null | grep -E "SHA1:|SHA256:" | sed 's/^/  /'

# ── Shrnutí ────────────────────────────────────────────────────────────────────
echo ""
echo "══════════════════════════════════════════════════"
info "Release build je připraven!"
echo ""
echo "  AAB pro Google Play:  $AAB_DEST"
[[ "$BUILD_APK" =~ ^[Yy]$ ]] && echo "  APK:                  $APK_DEST"
echo ""
echo "Další kroky:"
echo "  1. Přihlas se na https://play.google.com/console"
echo "  2. Vyber aplikaci → Vydání → Production → Nové vydání"
echo "  3. Nahraj soubor: $(basename "$AAB_DEST")"
echo "  4. Vyplň poznámky k vydání (store-listing/whats-new-cs.txt)"
echo "  5. Nastav zásady ochrany osobních údajů (store-listing/privacy-policy.html)"
echo "══════════════════════════════════════════════════"
