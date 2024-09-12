#!/usr/bin/env bash

TESTROOT="$( cd "$(dirname "$0")"/.. >/dev/null 2>&1 ; pwd -P )"
source $TESTROOT/files/testfunctions.sh
source $TESTROOT/files/test.conf
prepareTest $(basename $0) $*

configCar=$(makeTempFile $(basename $0) "\
ors:
  engine:
    profile_default:
      source_file:  ors-api/src/test/files/heidelberg.test.pbf
    profiles:
      driving-car:
        enabled: true")

configHgv=$(makeTempFile $(basename $0) "\
ors:
  engine:
    profile_default:
      source_file:  ors-api/src/test/files/heidelberg.test.pbf
    profiles:
      driving-hgv:
        enabled: true")

# A default yml ${CONTAINER_WORK_DIR}/ors-config.yml is present and would normally be used,
# but if a config file specified as run argument (first positional parameter)
# the run argument should win.
podman run --replace --name "${CONTAINER}" -p "${HOST_PORT}":8082 \
  -v "${M2_FOLDER}":/root/.m2 \
  -v "${TESTROOT}/graphs_volume":"${CONTAINER_WORK_DIR}/graphs" \
  -v "${configCar}":${CONTAINER_WORK_DIR}/ors-config.yml \
  -v "${configHgv}":${CONTAINER_WORK_DIR}/config-hgv.yml \
  "local/${IMAGE}:latest" \
  $(getProgramArguments ${runType} ${CONTAINER_WORK_DIR}/config-hgv.yml) &

awaitOrsReady 60 "${HOST_PORT}"
profiles=$(requestEnabledProfiles ${HOST_PORT})
cleanupTest

assertEquals "driving-hgv" "${profiles}"
