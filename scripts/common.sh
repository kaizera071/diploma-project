#!/bin/bash

# Build ingestion maven project
build_maven_project() {
    local pom_path="$1"
    mvn -f "$pom_path" clean install
}

# Function to build Docker image
build_docker_image() {
    local image_tag="$1"
    local doget_file_location="$2"

    echo "Building Docker image..."
    docker build -t "$image_tag" -f "$doget_file_location" .
}

# Function that wait until the pod is running
wait_for_pod_running() {
    local POD_NAME=$1
    echo "Waiting for pod $POD_NAME to be running..."
    while true; do
        POD_STATUS=$(kubectl get pod $POD_NAME -o jsonpath='{.status.phase}')
        if [[ $POD_STATUS == "Running" ]]; then
            echo "Pod $POD_NAME is now running."
            break
        elif [[ $POD_STATUS == "Pending" ]]; then
            echo "Pod $POD_NAME is still pending. Waiting..."
        else
            echo "Pod $POD_NAME is in state: $POD_STATUS"
            exit 1
        fi
        sleep 3 # Adjust the interval if needed
    done
}

# Function to apply a Resource YAML file with retry
apply_resource_with_retry() {
    local resource_yaml=$1
    local retries=3
    local delay=3

    for ((i = 1; i <= $retries; i++)); do
        echo "Attempt $i: Applying Resource from $resource_yaml..."
        kubectl apply -f "$resource_yaml"

        if [ $? -eq 0 ]; then
            echo "Resource applied successfully."
            return 0
        else
            echo "Resource application failed. Retrying in $delay seconds..."
            sleep $delay
        fi
    done

    echo "Failed to apply Resource after $retries attempts."
    return 1
}
