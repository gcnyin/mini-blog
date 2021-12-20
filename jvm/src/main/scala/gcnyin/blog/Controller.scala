package gcnyin.blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import gcnyin.blog.TapirEndpoint._
import gcnyin.blog.common.Dto.Message
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.server.interceptor.ValuedEndpointOutput
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.Future

class Controller(serviceLogic: ServiceLogic) {
  private def failureResponse(msg: String): ValuedEndpointOutput[_] =
    ValuedEndpointOutput(messageBody, Message(msg))

  private val serverOptions: AkkaHttpServerOptions =
    AkkaHttpServerOptions.customInterceptors
      .errorOutput(failureResponse)
      .options

  private val posts: Route =
    AkkaHttpServerInterpreter(serverOptions)
      .toRoute(postsEndpoint.serverLogic(_ => serviceLogic.getPostsWithoutContent))

  private val post: Route =
    AkkaHttpServerInterpreter(serverOptions)
      .toRoute(postEndpoint.serverLogic(serviceLogic.getPostById))

  private val createPost: Route =
    AkkaHttpServerInterpreter(serverOptions).toRoute(
      createPostEndpoint
        .serverSecurityLogic(serviceLogic.verifyToken)
        .serverLogic(_ => post => serviceLogic.savePostWithoutCreated(post))
    )

  private val createToken: Route =
    AkkaHttpServerInterpreter(serverOptions).toRoute(
      createTokenEndpoint
        .serverSecurityLogic(serviceLogic.verifyUsernamePassword)
        .serverLogic(username => _ => serviceLogic.createToken(username))
    )

  private val updatePost: Route =
    AkkaHttpServerInterpreter(serverOptions).toRoute(
      updatePostEndpoint
        .serverSecurityLogic(serviceLogic.verifyToken)
        .serverLogic(_ => request => serviceLogic.updatePost _ tupled request)
    )

  val deletePost: Route =
    AkkaHttpServerInterpreter(serverOptions).toRoute(
      deletePostEndpoint
        .serverSecurityLogic(serviceLogic.verifyToken)
        .serverLogic(_ => postId => serviceLogic.deletePost(postId))
    )

  private val updateUserPassword: Route =
    AkkaHttpServerInterpreter(serverOptions).toRoute(
      updatePasswordEndpoint
        .serverSecurityLogic(serviceLogic.verifyToken)
        .serverLogic(user => password => serviceLogic.updateUserPassword(user.username, password.password))
    )

  private val home: Route = AkkaHttpServerInterpreter(serverOptions).toRoute(
    filesGetServerEndpoint[Future]("")("static")
  )

  private val static: Route = AkkaHttpServerInterpreter(serverOptions).toRoute(
    filesGetServerEndpoint[Future]("static")("static")
  )

  private val apiList: List[AnyEndpoint] =
    List(
      postsEndpoint,
      postEndpoint,
      updatePostEndpoint,
      createPostEndpoint,
      deletePostEndpoint,
      createTokenEndpoint,
      updatePasswordEndpoint
    )

  private val swaggerRoute: List[ServerEndpoint[Any, Future]] =
    SwaggerInterpreter().fromEndpoints[Future](apiList, "Blog", "1.0")

  private val openApi: Route =
    AkkaHttpServerInterpreter(serverOptions).toRoute(swaggerRoute)

  val route: Route = encodeResponse {
    openApi ~ posts ~ post ~ createPost ~ updatePost ~ deletePost ~ createToken ~ updateUserPassword ~ home ~ static
  }
}
