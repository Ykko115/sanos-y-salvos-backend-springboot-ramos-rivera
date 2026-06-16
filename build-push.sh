#!/usr/bin/env bash
set -euo pipefail

DOCKER_HUB_USER="${DOCKER_HUB_USER:-nyko115}"
REPO="${DOCKER_HUB_USER}/sanos-y-salvos-backend"
IMAGE_TAG="${IMAGE_TAG:-latest}"

SERVICES=(usuario mascotas reportes apigateway)

echo "==> Repositorio: ${REPO}"
echo "==> Tag base    : ${IMAGE_TAG}"

for SERVICE in "${SERVICES[@]}"; do
  TAG="${REPO}:${SERVICE}-${IMAGE_TAG}"
  echo ""
  echo "--- Construyendo ${TAG} ---"
  docker build -t "${TAG}" "./${SERVICE}"
  echo "--- Publicando ${TAG} ---"
  docker push "${TAG}"
done

echo ""
echo "==> Listo. Imágenes publicadas en:"
echo "    https://hub.docker.com/r/${REPO}/tags"
