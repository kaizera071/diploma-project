# Desc: Stop the minikube cluster
eval $(minikube docker-env) --unset

minikube delete