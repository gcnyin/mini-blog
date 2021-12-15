package gcnyin.blog

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.Logger
import zio.logging._
import zio.logging.slf4j._
import zio.{ExitCode, ULayer, UManaged, URIO, ZIO, ZManaged}

import scala.concurrent.Future

object Main extends zio.App {
  private val logger = Logger[Main.type]

  private val env: ULayer[Logging] =
    Slf4jLogger.make { (_, message) => message }

  private val s =
    """.______    __        ______     ______
      ||   _  \  |  |      /  __  \   /  ____|
      ||  |_)  | |  |     |  |  |  | |  |  __
      ||   _  <  |  |     |  |  |  | |  | |_ |
      ||  |_)  | |  `----.|  `--'  | |  |__| |
      ||______/  |_______| \______/   \______|""".stripMargin

  s.split("\n").foreach(it => logger.info(it))

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    for {
      system <- actorSystemUManaged
      controller <- ZioLayer.controllerUManaged(system)
      httpServer <- httpServerManaged(system, controller)
      _ <- log.info(s"$httpServer").toManaged_
    } yield httpServer
  }.provideCustomLayer(env).useForever.exitCode

  def actorSystemUManaged: UManaged[ActorSystem[Unit]] =
    ZManaged.make(ZIO.succeed(ActorSystem(Behaviors.empty[Unit], "root"))) { sys =>
      ZIO.effectTotal(sys.terminate())
    }

  def httpServerManaged(
                         actorSystem: ActorSystem[Nothing],
                         controller: Controller
                       ): ZManaged[Any, Throwable, Http.ServerBinding] = {
    implicit val system: ActorSystem[Nothing] = actorSystem

    def httpServer(): Future[Http.ServerBinding] = {
      Http()
        .newServerAt("0.0.0.0", 8080)
        .bind(controller.route)
    }

    ZManaged.make(ZIO.fromFuture(_ => httpServer())) { http =>
      ZIO.fromFuture(_ => http.unbind()).orDie
    }
  }
}
