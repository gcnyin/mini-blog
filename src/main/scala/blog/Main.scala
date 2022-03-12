package blog

import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main {
  private val logger: Logger = Logger[Main.type]

  logger.info("application start")

  def main(args: Array[String]): Unit = {
    ActorSystem(rootActor, "mini-blog")
  }

  def rootActor: Behavior[Unit] = Behaviors.setup[Unit] { ctx =>
    implicit val system: ActorSystem[Nothing] = ctx.system
    implicit val ec: ExecutionContextExecutor = ctx.executionContext

    val postEventSourceActorRef: ActorRef[PostEventSourceBehavior.Command] =
      ctx.spawn(PostEventSourceBehavior(), "post-event-source-actor")
    val postService = new PostService(postEventSourceActorRef)
    val controller = new Controller(postService)

    Http()
      .newServerAt("0.0.0.0", 8080)
      .bind(controller.route)
      .onComplete {
        case Failure(exception) =>
          logger.error("error", exception)
          system.terminate()
        case Success(value) =>
          logger.info("{}", value)
      }

    Behaviors.empty
  }
}
