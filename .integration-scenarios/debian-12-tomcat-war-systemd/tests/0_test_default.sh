#!/usr/bin/env bash
#################################
# Description: Test the default container
#################################

# Define container name
SCRIPT_NAME=$(basename "$0")
export CONTAINER_NAME="tomcat-test-$SCRIPT_NAME"
export CONTAINER_IMAGE="${1:-'local/tomcat-test:latest'}"
export CONTAINER_ENGINE="podman"

# Get the absolute path of this script
START_DIRECTORY="$(
  cd "$(dirname "$0")" >/dev/null 2>&1 || exit 1
  pwd -P
)"



# Import logging and networking
. "$START_DIRECTORY/../helpers/1_logging.sh"
. "$START_DIRECTORY/../helpers/2_system.sh"
. "$START_DIRECTORY/../helpers/3_files.sh"
. "$START_DIRECTORY/../helpers/4_permissions.sh"
. "$START_DIRECTORY/../helpers/7_networking.sh"
. "$START_DIRECTORY/../helpers/8_openrouteservice.sh"

log_info "Running test suite $SCRIPT_NAME with container $CONTAINER_NAME from image $CONTAINER_IMAGE"
# Check if the container exists and remove it
if podman ps -a | grep -q $CONTAINER_NAME; then podman rm -f $CONTAINER_NAME; fi
# Start container
PORT=$(get_free_port)
podman run -d --name $CONTAINER_NAME --systemd true -p ${PORT}:8080 $CONTAINER_IMAGE

# Check the java version
check_java_version "17" || exit 1

# Check if the systemd service is enabled
check_systemd_service_enabled "openrouteservice" true || exit 1

# Wait for the container to start
wait_for_url "127.0.0.1:${PORT}/ors/v2/health" 100 200 1 5 || exit 1

# Check that one profile is loaded
check_number_of_profiles_loaded "127.0.0.1:${PORT}/ors/v2/status" 1 || exit 1

# Check that 'driving-car' is loaded
check_profile_loaded "127.0.0.1:${PORT}/ors/v2/status" "driving-car" true|| exit 1

# Check that the graphs are properly build
check_ownership "/home/ors/" "ors" "tomcat" || exit 1
check_ownership "/home/ors/graphs/" "ors" "tomcat" || exit 1
check_recursive_ownership "/home/ors/graphs/**" "tomcat" "tomcat" || exit 1

# Check that each of the following folders exist
folders_to_expect=(
  "/opt/tomcat/10/webapps/ors"
  "/opt/tomcat/10/temp"
  "/home/ors/graphs/driving-car"
  "/home/ors/elevation_cache"
)
for folder in "${folders_to_expect[@]}"; do
  check_folder_exists $folder true || exit 1
done

# Check that each of the following files exist
files_to_expect=(
  "/home/ors/ors-config.yml"
  "/home/ors/logs/ors.log"
  "/home/ors/setenv.sh"
  "/home/ors/files/heidelberg.osm.gz"
  "/opt/tomcat/10/bin/setenv.sh"
  "/opt/tomcat/10/webapps/ors.war"
  "/etc/systemd/system/openrouteservice.service"
)
for file in "${files_to_expect[@]}"; do
  check_file_exists $file true || exit 1
done

# assert export JAVA_OPTS="$JAVA_OPTS -Dors.engine.source_file=/home/ors/files/heidelberg.osm.gz" in file setenv.sh
check_line_in_file "export JAVA_OPTS=\"\$JAVA_OPTS -Dors.engine.profile_default.source_file=/home/ors/files/heidelberg.osm.gz\"" /home/ors/setenv.sh true || exit 1

# Assert source /home/ors/setenv.sh in file /opt/tomcat/10/bin/setenv.sh
check_line_in_file ". /home/ors/setenv.sh" /opt/tomcat/10/bin/setenv.sh true || exit 1

# Check that /opt/tomcat/10/temp/GeoTools folder doesnt exist yet
check_folder_exists "/opt/tomcat/10/temp/GeoTools" false || exit 1

# Check that the server responds with a 200 status code for an avoid polygon request
check_avoid_area_request "http://127.0.0.1:${PORT}/ors/v2/directions/driving-car/geojson" 200 || exit 1

# Check that  /opt/tomcat/10/temp/GeoTools folder exists now
check_folder_exists "/opt/tomcat/10/temp/GeoTools" true || exit 1