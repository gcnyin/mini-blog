package blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import sttp.tapir.AnyEndpoint
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

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

  private val deletePostRoute: Route =
    AkkaHttpServerInterpreter().toRoute(
      TapirEndpoint.deletePostEndpoint.serverLogic(postId => postService.deletePost(postId))
    )

  private val endpoints: List[AnyEndpoint] = List(
    TapirEndpoint.createPostEndpoint,
    TapirEndpoint.getPostByIdEndpoint,
    TapirEndpoint.getPostsEndpoint,
    TapirEndpoint.deletePostEndpoint
  )

  private val swaggerEndpoints: List[ServerEndpoint[Any, Future]] =
    SwaggerInterpreter().fromEndpoints[Future](endpoints, "mini-blog", "1.0")

  private val swaggerRoute: Route = AkkaHttpServerInterpreter().toRoute(swaggerEndpoints)


  val route: Route = encodeResponse {
    healthCheckRoute ~ createPostRoute ~ getPostByIdRoute ~ getPostsRoute ~ deletePostRoute ~ swaggerRoute
  }
}
