# mini-blog

[![Coverage Status](https://coveralls.io/repos/github/gcnyin/mini-blog/badge.svg?branch=master)](https://coveralls.io/github/gcnyin/mini-blog?branch=master)

Personal blog system powered by Akka, Tapir, Zio and MongoDB.

## Requirements

- Java 8+
- MongoDB

## Preparation

Install MongoDB.

```
docker-compose up -d
```

Run the scripts in the `mongo-migration` directory in the MongoDB console.

## Build

```
sbt clean stage
```

## Run

```
./target/universal/stage/bin/blog
```

Default username and password: `admin:123456`.

## Test

```
sbt test coverageReport
```

Publish coverage report to <https://coveralls.io/>.

```
sbt coveralls
```

## OpenApi

http://localhost:8080/docs/index.html?url=/docs/docs.yaml
