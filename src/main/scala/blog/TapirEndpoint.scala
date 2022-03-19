package blog

import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object TapirEndpoint {
  val messageBody: EndpointIO.Body[String, Message] = jsonBody[Message]
  val createPostBody: EndpointIO.Body[String, Request.CreatePost] = jsonBody[Request.CreatePost]
  val postBody: EndpointIO.Body[String, Response.Post] = jsonBody[Response.Post]
  val postsWithTitleBody: EndpointIO.Body[String, Seq[Response.PostTitle]] = jsonBody[Seq[Response.PostTitle]]

  private val basicEndpoint: Endpoint[Unit, Unit, Message, Unit, Any] = endpoint.in("api").errorOut(messageBody)

  val healthCheckEndpoint: Endpoint[Unit, Unit, Unit, Unit, Any] =
    endpoint
      .in("health-check")
      .get

  val createPostEndpoint: Endpoint[Unit, Request.CreatePost, Message, Unit, Any] =
    basicEndpoint
      .in("posts")
      .post
      .in(createPostBody)

  val getPostByIdEndpoint: Endpoint[Unit, String, Message, Response.Post, Any] =
    basicEndpoint
      .in("posts" / path[String]("postId"))
      .description("get post by postId")
      .get
      .out(postBody)

  val getPostsEndpoint: Endpoint[Unit, Unit, Message, Seq[Response.PostTitle], Any] =
    basicEndpoint
      .in("posts")
      .description("list all posts")
      .get
      .out(postsWithTitleBody)
}
