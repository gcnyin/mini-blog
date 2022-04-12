package blog

import akka.{Done, actor}
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object CreatePersistenceTable {
  def main(args: Array[String]): Unit = {
    import akka.persistence.jdbc.testkit.scaladsl.SchemaUtils

    val system = ActorSystem(Behaviors.empty[Unit], "roo")
    implicit val ec: ExecutionContextExecutor = system.executionContext
    implicit val system1: actor.ActorSystem = system.classicSystem

    val done: Future[Done] = SchemaUtils.createIfNotExists()

    done.onComplete {
      case Failure(exception) => println(exception)
      case Success(_)         => system.terminate()
    }
  }
}
