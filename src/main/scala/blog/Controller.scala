package blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

class Controller(postService: PostService) {
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
    createPostRoute ~ getPostByIdRoute ~ getPostsRoute
  }
}
