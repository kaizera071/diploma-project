#!/bin/bash

source scripts/common

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
    helm install my-kafka bitnami/kafka --values chart-values/kafka-zoo.values.yaml
    echo "Kafka installation completed."
}

install_nginx() {
    echo "Adding Nginx Helm repository..."
    helm repo add ingress-nginx https://kubernetes.github.io/ingress-nginx
    echo "Updating Helm repositories..."
    helm repo update
    echo "Installing Nginx via Helm chart..."
    helm install my-nginx-ingress ingress-nginx/ingress-nginx --set controller.extraLabels.app=nginx-ingress
    echo "Nginx installation completed."
}

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

# Start Minikube if not already running
start_minikube

# Set up Minikube Docker environment
setup_minikube_docker_env

# Install Kafka
install_kafka

# Build ingestion service
build_maven_project audit-system-parent/ingestion/pom.xml

# Build ingestion Docker image
build_docker_image "ingestion:07.07.2024" audit-system-parent/ingestion/k8s/Dockerfile

# Deploy ingestion component
apply_resource_with_retry "audit-system-parent/ingestion/k8s/deployment.yaml"
apply_resource_with_retry "audit-system-parent/ingestion/k8s/service.yaml"

# Deploy audit system ingress
apply_resource_with_retry "audit-system-parent/ingress.yaml"

# Install Nginx
install_nginx

# Get the name of the Nginx pod
POD_NAME=$(kubectl get pods -l app.kubernetes.io/instance=my-nginx-ingress -o jsonpath='{.items[0].metadata.name}')

# Wait for the pod to become running
wait_for_pod_running "$POD_NAME"

# Port forward the pod
kubectl port-forward $POD_NAME 8080:80 &
