package com.github.gcnyin.blog

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorSystem, Behavior}
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger

import java.time.Instant
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main {
  private val logger = Logger[Main.type]

  private val s =
    """.______    __        ______     ______
      ||   _  \  |  |      /  __  \   /  ____|
      ||  |_)  | |  |     |  |  |  | |  |  __
      ||   _  <  |  |     |  |  |  | |  | |_ |
      ||  |_)  | |  `----.|  `--'  | |  |__| |
      ||______/  |_______| \______/   \______|""".stripMargin

  s.split("\n").foreach(it => logger.info(it))

  def main(args: Array[String]): Unit = {
    ActorSystem(root(), "root")
  }

  def root(): Behavior[Unit] = Behaviors.setup[Unit] { ctx =>
    implicit val system: ActorSystem[Nothing] = ctx.system
    implicit val context: ExecutionContextExecutor = system.executionContext

    val port = System.getProperty("http.port", "8080").toInt
    val mongoClient = new MongoClient()
    val serviceLogic = new ServiceLogic(mongoClient, new JwtComponent("!OD%0RWT01Fkq!", Instant.now))
    val controller = new Controller(serviceLogic)

    Http()
      .newServerAt("0.0.0.0", port)
      .bindFlow(controller.route)
      .onComplete {
        case Failure(exception) =>
          logger.error("Failed to start server", exception)
        case Success(value) =>
          logger.info("{}", value)
      }

    Behaviors.empty
  }
}
