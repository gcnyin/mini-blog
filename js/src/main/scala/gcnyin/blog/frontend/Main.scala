package gcnyin.blog.frontend

import gcnyin.blog.common.Dto._
import org.scalajs.dom
import org.scalajs.dom.{Element, document}
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

  val main: Element = document.querySelector("#main")
  val buttonZone: Element = document.querySelector("#button-zone")
  val postsZone: Element = document.querySelector("#posts-zone")
  val postZone: Element = document.querySelector("#post-zone")

  def main(args: Array[String]): Unit = {
    document.addEventListener(
      "DOMContentLoaded",
      { (e: dom.Event) =>
        setupUI()
      }
    )
  }

  def setupUI(): Unit = {
    val ul = document.createElement("ul")
    ul.classList.add("list-group")

    val button = document.createElement("button")
    button.classList.add("btn")
    button.classList.add("btn-primary")
    button.textContent = "Fetch posts"
    button.addEventListener(
      "click",
      (_: dom.MouseEvent) => {
        val showPosts = for {
          resp <- ZIO.fromFuture(implicit ec => fetchBackend.send(HttpClient.getPosts()))
          _ <- log.info(s"$resp")
          posts <- ZIO.fromEither(resp.body match {
            case _: DecodeResult.Failure => Left[Message, Seq[PostWithoutContent]](Message("failure"))
            case DecodeResult.Value(v)   => v
          })
          _ <- ZIO.effectTotal(appendPostsWithoutContent(ul, posts))
        } yield ()
        Runtime.default.unsafeRunAsync_(showPosts.provideCustomLayer(loggingEnv))
      }
    )
    buttonZone.appendChild(button)
    postsZone.appendChild(ul)
  }

  def appendPostsWithoutContent(targetNode: dom.Node, posts: Seq[PostWithoutContent]): Unit = {
    targetNode.textContent = ""
    for (p <- posts) {
      appendPostWithoutContent(targetNode, p)
    }
  }

  def appendPostWithoutContent(targetNode: dom.Node, post: PostWithoutContent): Unit = {
    val parNode = document.createElement("li")
    parNode.textContent = post.title
    parNode.addEventListener(
      "click",
      (_: dom.MouseEvent) => {
        val showPosts = for {
          resp <- ZIO.fromFuture(implicit ec => fetchBackend.send(HttpClient.getPost(post.id)))
          _ <- log.info(s"$resp")
          post <- ZIO.fromEither(resp.body match {
            case _: DecodeResult.Failure => Left[Message, Post](Message("failure"))
            case DecodeResult.Value(v)   => v
          })
          _ <- ZIO.effectTotal(showPost(postZone, post))
        } yield ()
        Runtime.default.unsafeRunAsync_(showPosts.provideCustomLayer(loggingEnv))
      }
    )
    parNode.classList.add("list-group-item")
    targetNode.appendChild(parNode)
  }

  def showPost(targetNode: dom.Node, post: Post): Unit = {
    targetNode.textContent = ""
    val h1 = document.createElement("h1")
    h1.textContent = post.title
    targetNode.appendChild(h1)
    post.content.split("\n").foreach { line =>
      val p = document.createElement("p")
      p.textContent = line
      targetNode.appendChild(p)
    }
  }
}
