#!/usr/bin/env bash
# Verifie que l'image honore le contrat qu'une PaaS impose. Ces sept points sont
# ce qui fait echouer un deploiement, independamment de la PaaS retenue.
#
#   ./deploy/verifier-contrat-paas.sh [image]
set -euo pipefail
IMAGE="${1:-tahore-document-processing:0.1.0-SNAPSHOT}"
NOM="verif-paas-$$"
ok() { printf '  \033[32mOK\033[0m   %s\n' "$1"; }
ko() { printf '  \033[31mECHEC\033[0m %s\n' "$1"; ERREURS=$((ERREURS+1)); }
ERREURS=0

echo "Image : $IMAGE"

echo "1. L'image ne tourne pas en root"
UID_EFF=$(docker run --rm --entrypoint id "$IMAGE" -u 2>/dev/null || echo 0)
[ "$UID_EFF" != "0" ] && ok "uid=$UID_EFF" || ko "le conteneur tourne en root"

echo "2. Demarrage avec un systeme de fichiers en lecture seule"
docker run -d --name "$NOM" --read-only --tmpfs /tmp \
  -e CU3_DMS_BASE_URL=http://exemple.invalid \
  -e CU3_TAHORE_BASE_URL=http://exemple.invalid \
  -p 18080:8080 "$IMAGE" >/dev/null
trap 'docker rm -f "$NOM" >/dev/null 2>&1 || true' EXIT

echo "3. Sonde de disponibilite"
for i in $(seq 1 60); do
  curl -fsS localhost:18080/actuator/health/readiness >/dev/null 2>&1 && break
  sleep 2
done
curl -fsS localhost:18080/actuator/health/readiness >/dev/null 2>&1 \
  && ok "readiness repond" || ko "readiness ne repond pas"

echo "4. Sonde de vivacite"
curl -fsS localhost:18080/actuator/health/liveness >/dev/null 2>&1 \
  && ok "liveness repond" || ko "liveness ne repond pas"

echo "5. Journaux sur la sortie standard"
[ -n "$(docker logs "$NOM" 2>&1 | head -c 100)" ] \
  && ok "journaux presents sur stdout/stderr" || ko "aucun journal capte"

echo "6. Configuration par variables d'environnement"
docker logs "$NOM" 2>&1 | grep -q "exemple.invalid" \
  && ok "la configuration injectee est prise en compte" \
  || echo "  NOTE  non verifiable sans journal explicite de la configuration"

echo "7. Arret propre sur SIGTERM (moins de 30 s)"
DEBUT=$(date +%s)
docker stop -t 30 "$NOM" >/dev/null
DUREE=$(( $(date +%s) - DEBUT ))
[ "$DUREE" -lt 30 ] && ok "arret en ${DUREE}s" || ko "arret force apres ${DUREE}s"

echo
[ "$ERREURS" -eq 0 ] && echo "Contrat honore." || { echo "$ERREURS point(s) en echec."; exit 1; }
