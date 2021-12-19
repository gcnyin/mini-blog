package gcnyin.blog.frontend

import gcnyin.blog.common.Dto
import gcnyin.blog.common.Dto.Message
import org.scalajs.dom
import org.scalajs.dom.document
import sttp.capabilities
import sttp.client3._
import sttp.tapir.DecodeResult
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.logging._
import zio.logging.js.ConsoleLogger

import java.util.TimeZone
import scala.concurrent.Future

object Main {

  /** @note There is no other [[java.util.TimeZone]] in the web browser, such as Asia/Shanghai */
  TimeZone.setDefault(
    TimeZone.getTimeZone("UTC")
  )

  val fetchBackend: SttpBackend[Future, capabilities.WebSockets] = FetchBackend()

  val loggingEnv: ZLayer[Console with Clock, Nothing, Logging] =
    ConsoleLogger.make()

  def main(args: Array[String]): Unit = {
    document.addEventListener(
      "DOMContentLoaded",
      { (e: dom.Event) =>
        setupUI()
      }
    )
  }

  def setupUI(): Unit = {
    val ol = document.createElement("ol")
    val button = document.createElement("button")
    button.classList.add("btn")
    button.classList.add("btn-primary")
    button.textContent = "Fetch posts"
    button.addEventListener(
      "click",
      { (_: dom.MouseEvent) =>
        val showPosts = for {
          resp <- ZIO.fromFuture(implicit ec => fetchBackend.send(HttpClient.getPosts()))
          _ <- log.info(s"$resp")
          posts <- ZIO.fromEither(resp.body match {
            case _: DecodeResult.Failure => Left[Dto.Message, Seq[Dto.PostWithoutContent]](Message("failure"))
            case DecodeResult.Value(v)   => v
          })
          _ <- ZIO.effectTotal(appendPostsWithoutContent(ol, posts))
        } yield ()
        Runtime.default.unsafeRunAsync_(showPosts.provideCustomLayer(loggingEnv))
      }
    )
    document.body.appendChild(button)
    document.body.append(ol)
  }

  def appendPostsWithoutContent(targetNode: dom.Node, posts: Seq[Dto.PostWithoutContent]): Unit = {
    for (p <- posts) {
      appendPostWithoutContent(targetNode, p)
    }
  }

  def appendPostWithoutContent(targetNode: dom.Node, post: Dto.PostWithoutContent): Unit = {
    val parNode = document.createElement("li")
    parNode.textContent = post.title
    targetNode.appendChild(parNode)
  }
}
