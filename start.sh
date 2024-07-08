#!/bin/bash

# Function to start Minikube if not running
start_minikube() {
    if ! minikube status &>/dev/null; then
        echo "Starting Minikube..."
        minikube start -p minikube --memory=8192 --cpus=4
    else
        echo "Minikube is already running."
    fi
    # Enable Minikube addons
    minikube addons enable metrics-server
}

# Function to set up Minikube Docker environment
setup_minikube_docker_env() {
    echo "Setting up Minikube Docker environment..."
    eval $(minikube -p minikube docker-env)
}

# Function to install Kafka
install_kafka() {
    echo "Adding Bitnami Helm repository..."
    helm repo add bitnami https://charts.bitnami.com/bitnami
    echo "Updating Helm repositories..."
    helm repo update
    echo "Installing Kafka via Helm chart..."
    helm install my-kafka bitnami/kafka --values kafka-zoo.values.yaml
    echo "Kafka installation completed."
}

# Build ingestion maven project  
build_ingestion_service() {
    mvn -f audit-system-parent/ingestion/pom.xml clean install
}

# Function to build Docker image
build_docker_image() {
    local image_tag="$1"
    local doget_file_location="$2"

    echo "Building Docker image..."
    docker build -t "$image_tag" -f "$doget_file_location" .
}

# Function to deploy Docker image in Minikube
deploy_to_minikube() {
    local deployment_file="$1"
    local service_file="$2"

    echo "Deploying to Minikube..."
    kubectl apply -f "$deployment_file"
    kubectl apply -f "$service_file"
}

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
        sleep 3  # Adjust the interval as needed
    done
}

apply_resource_with_retry() {
    local resource_yaml=$1  # Path to Resource YAML file passed as argument
    local retries=3         # Number of retry attempts
    local delay=3          # Delay between retries in seconds

    for (( i=1; i<=$retries; i++ )); do
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

# Start Minikube if not already running
start_minikube

# Set up Minikube Docker environment
setup_minikube_docker_env

# Install Kafka
install_kafka

# Build ingestion service
build_ingestion_service

# Build Docker image
build_docker_image "ingestion:07.07.2024" audit-system-parent/ingestion/k8s/Dockerfile

# Deploy to Minikube
deploy_to_minikube "audit-system-parent/ingestion/k8s/deployment.yaml" "audit-system-parent/ingestion/k8s/service.yaml"
apply_resource_with_retry "audit-system-parent/ingress.yaml"

helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
helm install my-nginx-ingress ingress-nginx/ingress-nginx --set controller.extraLabels.app=nginx-ingress

POD_NAME=$(kubectl get pods -l app.kubernetes.io/instance=my-nginx-ingress -o jsonpath='{.items[0].metadata.name}')

# Wait for the pod to become running
wait_for_pod_running "$POD_NAME"

kubectl port-forward $POD_NAME 8080:80
