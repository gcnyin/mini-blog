# mini-blog

[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.8.0.svg)](https://www.scala-js.org)
[![Coverage Status](https://coveralls.io/repos/github/gcnyin/mini-blog/badge.svg?branch=master)](https://coveralls.io/github/gcnyin/mini-blog?branch=master)

Personal blog system powered by Akka, Tapir, Zio, Cats and MongoDB.

## Requirements

- Java 8+
- MongoDB

## Functions

- Get post list
- Create post
- Update post
- Delete post
- Create token
- Update password

## Preparation

Install MongoDB.

```
docker-compose up -d
```

Run the scripts in the `mongo-migration` directory in the MongoDB console.

## Build

```shell
sbt stage
```

## Run

```shell
./target/universal/stage/bin/mini-blog
```

Open `http://localhost:8080` in web browser.

**Frontend is still WIP.**

## Auth

Use jwt.

Default username and password: `admin:123456`.

Use `src/main/password.sc` to generate a new encoded password if you want to change the password in Mongodb manually.

## OpenApi

http://localhost:8080/docs/index.html?url=/docs/docs.yaml

## Postman

Import collection json in `postman` directory.
