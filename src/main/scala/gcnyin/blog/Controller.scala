package gcnyin.blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import Model.Message
import TapirEndpoint._
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
        .serverLogic(userWithoutPassword => post => serviceLogic.savePostWithoutCreated(userWithoutPassword, post))
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

  private val apiList: List[AnyEndpoint] =
    List(postsEndpoint, postEndpoint, updatePostEndpoint, createPostEndpoint, createTokenEndpoint)

  private val swaggerRoute: List[ServerEndpoint[Any, Future]] =
    SwaggerInterpreter().fromEndpoints[Future](apiList, "Blog", "1.0")

  private val openApi: Route =
    AkkaHttpServerInterpreter(serverOptions).toRoute(swaggerRoute)

  val route: Route = encodeResponse {
    openApi ~ posts ~ post ~ createPost ~ updatePost ~ createToken
  }
}
