#!/usr/bin/env bash
set -euo pipefail

DOCKER_HUB_USER="${DOCKER_HUB_USER:-nyko115}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

SERVICES=(usuario mascotas reportes apigateway)

echo "==> Construyendo y publicando imágenes como ${DOCKER_HUB_USER}/*:${IMAGE_TAG}"

for SERVICE in "${SERVICES[@]}"; do
  IMAGE="${DOCKER_HUB_USER}/sanos-salvos-${SERVICE}:${IMAGE_TAG}"
  echo ""
  echo "--- Construyendo ${IMAGE} ---"
  docker build -t "${IMAGE}" "./${SERVICE}"
  echo "--- Publicando ${IMAGE} ---"
  docker push "${IMAGE}"
done

echo ""
echo "==> Listo. Imágenes publicadas en Docker Hub:"
for SERVICE in "${SERVICES[@]}"; do
  echo "    https://hub.docker.com/r/${DOCKER_HUB_USER}/sanos-salvos-${SERVICE}"
done
