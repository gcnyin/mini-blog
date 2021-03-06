# mini-blog

Blog system powered by scala/akka.

## Setup development environment

```
docker-compose up -d
```

## Deploy to k8s

```shell
sbt docker:publishLocal # package application
kubectl apply -f src/main/resources/k8s/cert-manager.yaml # create cert manager
kubectl apply -f src/main/resources/k8s/jaeger-operator.yaml # create jaeger operator 
kubectl apply -f src/main/resources/k8s/jaeger-instance.yml # create jaeger instance
kubectl apply -f src/main/resources/k8s/postgres.yml # deploy postgres database
kubectl apply -f src/main/resources/k8s/application.yml # deploy application
```

## OpenAPI

http://localhost:8080/docs/index.html?url=/docs/docs.yaml
