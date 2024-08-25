#!/bin/bash

source common.sh

cd ..

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
    helm install kafka oci://registry-1.docker.io/bitnamicharts/kafka
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

install_minio() {
    echo "Installing Minio via Helm"
    helm install minio oci://registry-1.docker.io/bitnamicharts/minio
}

# Function to build maven project
build_maven_project() {
    local pom_path="$1"
    mvn -f "$pom_path" clean install
}

# Function to build Docker image
build_docker_image() {
    local image_tag="$1"
    local docker_file_location="$2"

    echo "Building Docker image..."
    docker build -t "$image_tag" -f "$docker_file_location" .
}

# Start Minikube if not already running
start_minikube

# Set up Minikube Docker environment
setup_minikube_docker_env

# Install Kafka
install_kafka

# Install Minio
install_minio

# Create a secret for the AES key
kubectl create secret generic aes-key --from-literal=key=$(openssl rand -base64 32)

# Build ingestion service
build_maven_project audit-system-parent/ingestion/pom.xml

# Build ingestion Docker image
build_docker_image "ingestion:18.07.2024" audit-system-parent/ingestion/k8s/Dockerfile

# Deploy ingestion component
apply_resource_with_retry "audit-system-parent/ingestion/k8s/deployment.yaml"
apply_resource_with_retry "audit-system-parent/ingestion/k8s/service.yaml"

# Build retrieval service
build_maven_project audit-system-parent/retrieval/pom.xml

# Build retrieval Docker image
build_docker_image "retrieval:18.07.2024" audit-system-parent/retrieval/k8s/Dockerfile

# Deploy retrieval component
apply_resource_with_retry "audit-system-parent/retrieval/k8s/deployment.yaml"
apply_resource_with_retry "audit-system-parent/retrieval/k8s/service.yaml"

# Build forwarder service
build_maven_project audit-system-parent/forwarder/pom.xml

# Build forwarder Docker image
build_docker_image "forwarder:18.07.2024" audit-system-parent/forwarder/k8s/Dockerfile

# Deploy forwarder component
apply_resource_with_retry "audit-system-parent/forwarder/k8s/deployment.yaml"

# Deploy audit system ingress
apply_resource_with_retry "audit-system-parent/ingress.yaml"

# Install Nginx
install_nginx

# Get the name of the Nginx pod
NGINX_POD_NAME=$(kubectl get pods -l app.kubernetes.io/instance=my-nginx-ingress -o jsonpath='{.items[0].metadata.name}')
# Wait for the nginx pod to become running
wait_for_pod_running "$NGINX_POD_NAME"

# Get the name of the Minio pod
MINIO_POD_NAME=$(kubectl get pods -l app.kubernetes.io/instance=minio -o jsonpath='{.items[0].metadata.name}')
# Wait for the Minio pod to become running
wait_for_pod_running "$MINIO_POD_NAME"

# Port forward the pod
kubectl port-forward $NGINX_POD_NAME 8080:80 &
kubectl port-forward svc/minio 9001:9001 &
