# mini-blog

Blog system powered by scala/akka.

## Deploy to k8s

Create and edit `application-prod.conf` in `src/main/resources`, and then run these commands.

```shell
sbt docker:publishLocal # package application
kubectl apply -f src/main/resources/k8s/cert-manager.yaml # create cert manager
kubectl apply -f src/main/resources/k8s/jaeger-operator.yaml # create jaeger operator 
kubectl apply -f src/main/resources/k8s/jaeger-instance.yml # create jaeger instance
kubectl apply -f src/main/resources/k8s/application.yml # deploy application
```
