#!/usr/bin/env bash
# Skript pro vygenerování podpisového keystoru pro Google Play.
# Spusť JEDNOU – keystore uchovej na bezpečném místě a NEZVEŘEJŇUJ.
set -euo pipefail

# ── Konfigurace ────────────────────────────────────────────────────────────────
KEY_ALIAS="stoic-key"
KEYSTORE_FILE="stoic-release.jks"
VALIDITY_DAYS=10000          # ~27 let, Google Play vyžaduje min. 2033

# ── Bezpečnostní upozornění ────────────────────────────────────────────────────
echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║  DŮLEŽITÉ: Bezpečnost keystoru                          ║"
echo "║  - Nikdy neverzuj keystore do gitu                      ║"
echo "║  - Zálohuj keystore na bezpečné místo (offline záloha)  ║"
echo "║  - Zapamatuj si heslo – BEZ NĚJ NELZE AKTUALIZOVAT APP ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# ── Kontrola, zda keystore již existuje ───────────────────────────────────────
if [ -f "$KEYSTORE_FILE" ]; then
  echo "CHYBA: Soubor '$KEYSTORE_FILE' již existuje."
  echo "Pokud chceš vygenerovat nový keystore, přejmenuj nebo odstraň stávající."
  exit 1
fi

# ── Zadání metadat ─────────────────────────────────────────────────────────────
echo "Zadej údaje pro certifikát:"
read -rp "Jméno a příjmení (CN): " CERT_CN
read -rp "Organizace (O) [např. Osobní]: " CERT_O
read -rp "Město (L): " CERT_L
read -rp "Kraj / stát (ST): " CERT_ST
read -rp "Kód země (C) [např. CZ]: " CERT_C

DNAME="CN=${CERT_CN}, O=${CERT_O}, L=${CERT_L}, ST=${CERT_ST}, C=${CERT_C}"

echo ""
read -rsp "Zadej heslo keystoru (min. 6 znaků): " KS_PASS
echo ""
read -rsp "Potvrď heslo keystoru: " KS_PASS2
echo ""

if [ "$KS_PASS" != "$KS_PASS2" ]; then
  echo "CHYBA: Hesla se neshodují."
  exit 1
fi

# ── Generování keystoru ────────────────────────────────────────────────────────
echo ""
echo "Generuji keystore '$KEYSTORE_FILE' ..."

keytool -genkeypair \
  -v \
  -keystore "$KEYSTORE_FILE" \
  -alias "$KEY_ALIAS" \
  -keyalg RSA \
  -keysize 4096 \
  -validity "$VALIDITY_DAYS" \
  -storepass "$KS_PASS" \
  -keypass "$KS_PASS" \
  -dname "$DNAME"

echo ""
echo "✓ Keystore vygenerován: $KEYSTORE_FILE"
echo "  Alias: $KEY_ALIAS"
echo ""

# ── Uložení konfigurace do keystore.properties ────────────────────────────────
PROPS_FILE="../keystore.properties"
echo "Ukládám konfiguraci do $PROPS_FILE ..."

cat > "$PROPS_FILE" <<EOF
# SOUBOR OBSAHUJE CITLIVÉ ÚDAJE – NIKDY NEVERZUJ DO GITU!
storeFile=../scripts/${KEYSTORE_FILE}
storePassword=${KS_PASS}
keyAlias=${KEY_ALIAS}
keyPassword=${KS_PASS}
EOF

echo "✓ Konfigurace uložena do $PROPS_FILE"
echo ""
echo "Zkontroluj, že keystore.properties je v .gitignore!"
echo ""
echo "Přidej do app/build.gradle.kts:"
echo ""
echo "  val keystoreProps = Properties().apply {"
echo "      load(rootProject.file(\"keystore.properties\").reader())"
echo "  }"
echo "  signingConfigs {"
echo "      create(\"release\") {"
echo "          storeFile = file(keystoreProps[\"storeFile\"] as String)"
echo "          storePassword = keystoreProps[\"storePassword\"] as String"
echo "          keyAlias = keystoreProps[\"keyAlias\"] as String"
echo "          keyPassword = keystoreProps[\"keyPassword\"] as String"
echo "      }"
echo "  }"
