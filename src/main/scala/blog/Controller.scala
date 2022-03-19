package blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

import scala.concurrent.Future

class Controller(postService: PostService) {
  private val healthCheckRoute: Route = AkkaHttpServerInterpreter().toRoute(
    TapirEndpoint.healthCheckEndpoint.serverLogic(_ => {
      val value: Either[Unit, Unit] = Right(())
      Future.successful(value)
    })
  )

  private val createPostRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      TapirEndpoint.createPostEndpoint.serverLogic(r => postService.createPost(r.title, r.content))
    )

  private val getPostByIdRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      TapirEndpoint.getPostByIdEndpoint.serverLogic(r => postService.getPostByPostId(r))
    )

  private val getPostsRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      TapirEndpoint.getPostsEndpoint.serverLogic(_ => postService.getPosts)
    )

  val route: Route = encodeResponse {
    healthCheckRoute ~ createPostRoute ~ getPostByIdRoute ~ getPostsRoute
  }
}
