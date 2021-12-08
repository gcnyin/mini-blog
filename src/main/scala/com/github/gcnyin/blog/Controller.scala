package com.github.gcnyin.blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.gcnyin.blog.TapirEndpoint._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter
import scala.concurrent.Future
import sttp.tapir.model.UsernamePassword
import sttp.tapir.server.PartialServerEndpoint

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

  val route: Route = posts ~ createPost ~ createToken
}
