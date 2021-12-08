# mini-blog

Powered by Akka, Tapir and MongoDB.

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
