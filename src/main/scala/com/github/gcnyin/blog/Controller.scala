package com.github.gcnyin.blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.gcnyin.blog.TapirEndpoint._
import sttp.tapir._
import sttp.tapir.server.ServerEndpoint
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import sttp.tapir.swagger.bundle.SwaggerInterpreter

import scala.concurrent.Future

class Controller(serviceLogic: ServiceLogic) {
  private val posts: Route =
    AkkaHttpServerInterpreter()
      .toRoute(postsEndpoint.serverLogic(_ => serviceLogic.getPostsWithoutContent))

  private val createPost: Route =
    AkkaHttpServerInterpreter().toRoute(
      createPostEndpoint
        .serverSecurityLogic(token => serviceLogic.verifyToken(token))
        .serverLogic(userWithoutPassword => post => serviceLogic.savePostWithoutCreated(userWithoutPassword, post))
    )

  private val createToken: Route =
    AkkaHttpServerInterpreter().toRoute(
      createTokenEndpoint
        .serverSecurityLogic(usernamePassword => serviceLogic.verifyUsernamePassword(usernamePassword))
        .serverLogic(username => _ => serviceLogic.createToken(username))
    )

  private val apiList: List[AnyEndpoint] = List(postsEndpoint, createPostEndpoint, createTokenEndpoint)

  private val swaggerRoute: List[ServerEndpoint[Any, Future]] =
    SwaggerInterpreter().fromEndpoints[Future](apiList, "Blog", "1.0")

  private val openApi: Route = AkkaHttpServerInterpreter().toRoute(swaggerRoute)

  val route: Route = openApi ~ posts ~ createPost ~ createToken
}
