package gcnyin.blog.frontend

import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.macrotaskexecutor.MacrotaskExecutor.Implicits._
import sttp.capabilities
import sttp.client3._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main {
  val fetchBackend: SttpBackend[Future, capabilities.WebSockets] = FetchBackend()

  def main(args: Array[String]): Unit = {
    document.addEventListener(
      "DOMContentLoaded",
      { (e: dom.Event) =>
        setupUI()
      }
    )
  }

  def setupUI(): Unit = {
    val button = document.createElement("button")
    button.textContent = "Fetch posts"
    button.addEventListener(
      "click",
      { (e: dom.MouseEvent) =>
        fetchBackend
          .send(HttpClient.getPosts())
          .onComplete {
            case Failure(exception) => println(exception)
            case Success(value)     => {
              appendPar(document.body, value.toString)
            }
          }
      }
    )
    document.body.appendChild(button)
  }

  def appendPar(targetNode: dom.Node, text: String): Unit = {
    val parNode = document.createElement("p")
    parNode.textContent = text
    targetNode.appendChild(parNode)
  }
}
