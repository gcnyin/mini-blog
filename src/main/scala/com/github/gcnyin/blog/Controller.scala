package com.github.gcnyin.blog

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.gcnyin.blog.TapirEndpoint._
import sttp.tapir.server.akkahttp.AkkaHttpServerInterpreter

class Controller(serviceLogic: ServiceLogic) {
  private val postsRoute: Route =
    AkkaHttpServerInterpreter()
      .toRoute(postsEndpoint.serverLogic(_ => serviceLogic.getPostsWithoutContent))

  private val createPostRoute: Route =
    AkkaHttpServerInterpreter()
      .toRoute(createPostEndpoint.serverLogic(serviceLogic.savePostWithoutCreated))

  val route: Route = postsRoute ~ createPostRoute
}
