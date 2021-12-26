package gcnyin.blog

import akka.http.scaladsl.server.Route
import gcnyin.blog.common.Dto.Message
import gcnyin.blog.common.TapirEndpoint._
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.{AkkaHttpServerInterpreter, AkkaHttpServerOptions}
import sttp.tapir.server.interceptor.ValuedEndpointOutput
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.Future
import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.model.ContentTypes
import scala.util.Failure
import scala.util.Success

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

  import akka.http.scaladsl.server.Directives._

  private val home = path("") {
    get {
      onSuccess(serviceLogic.getPostsWithoutContent) {
        case Left(e) =>
          complete(HttpEntity(ContentTypes.`application/json`, s"""{"msg": ${e.msg}}"""))
        case Right(posts) =>
          import scalatags.Text.all._

          val indexHtml = html(
            head(
              title := "Mini Blog"
            ),
            body(
              h1("Mini Blog"),
              ul(
                for (p <- posts) yield li(p.title)
              )
            )
          ).render

          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, indexHtml))
      }
    }
  }

  val route: Route = encodeResponse {
    home ~ openApi ~ posts ~ post ~ createPost ~ updatePost ~ deletePost ~ createToken ~ updateUserPassword
  }
}
