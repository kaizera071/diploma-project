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
    local service_name="$2"

    echo "Deploying to Minikube..."
    kubectl apply -f "$deployment_file"

    kubectl apply -f "audit-system-parent/ingestion/k8s/service.yaml"

    #echo "Exposing service..."
    #kubectl expose deployment "$service_name" --type=NodePort --port=80
}

# Function to get service URL
get_service_url() {
    local service_name="$1"

    echo "Service URL:"
    minikube service "$service_name" --url
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
deploy_to_minikube "audit-system-parent/ingestion/k8s/deployment.yaml" "ingestion"

# Get service URL
get_service_url "ingestion"
