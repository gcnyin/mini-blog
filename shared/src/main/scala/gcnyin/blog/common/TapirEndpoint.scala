package gcnyin.blog.common

import gcnyin.blog.common.Dto._
import io.circe.generic.auto._
import sttp.tapir.Validator.{MaxLength, MinLength}
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.model.UsernamePassword

object TapirEndpoint {
  val messageBody: EndpointIO.Body[String, Message] =
    jsonBody[Message]
      .description("Something happen")
      .example(Message("Post saved successfully"))

  private val postBody: EndpointIO.Body[String, Post] =
    jsonBody[Post]
      .description("Post")
      .example(Post("61b0d8183b6a98374bc4059d", "title", "content", System.currentTimeMillis()))

  private val postUpdateBody: EndpointIO.Body[String, PostUpdateBody] =
    jsonBody[PostUpdateBody]
      .description("post to update")
      .example(PostUpdateBody("61b0d8183b6a98374bc4059d", "title", "content"))

  private val postWithoutContentSeqBody: EndpointIO.Body[String, Seq[PostWithoutContent]] =
    jsonBody[Seq[PostWithoutContent]]
      .description("Post without content")
      .example(Seq(PostWithoutContent("61b0d8183b6a98374bc4059d", "title", System.currentTimeMillis())))

  private val postWithoutCreatedBody: EndpointIO.Body[String, PostWithoutCreated] =
    jsonBody[PostWithoutCreated]
      .description("Post that has not been saved")

  private val tokenBody: EndpointIO.Body[String, Token] =
    jsonBody[Token]
      .description("Json Web Token")

  private val newPasswordBody: EndpointIO.Body[String, NewPassword] =
    jsonBody[NewPassword]
      .description("new password")
      .validate(MinLength(12).contramap(_.password))
      .validate(MaxLength(24).contramap(_.password))
      .example(NewPassword("qEcve@Q734f*#6vK"))

  private val basicEndpoint: Endpoint[Unit, Unit, Message, Unit, Any] =
    endpoint
      .in("api")
      .errorOut(messageBody)

  private val jwtAuthEndpoint: Endpoint[String, Unit, Message, Unit, Any] =
    basicEndpoint
      .securityIn(auth.bearer[String]())

  private val basicAuthEndpoint: Endpoint[UsernamePassword, Unit, Message, Unit, Any] =
    basicEndpoint
      .securityIn(auth.basic[UsernamePassword]())

  val createTokenEndpoint: Endpoint[UsernamePassword, Unit, Message, Token, Any] =
    basicAuthEndpoint
      .in("tokens")
      .description("create token")
      .post
      .out(tokenBody)

  val postsEndpoint: Endpoint[Unit, Unit, Message, Seq[PostWithoutContent], Any] =
    basicEndpoint
      .in("posts")
      .description("posts")
      .get
      .out(postWithoutContentSeqBody)

  val postEndpoint: Endpoint[Unit, String, Message, Post, Any] =
    basicEndpoint
      .in("posts" / path[String]("postId"))
      .description("post")
      .get
      .out(postBody)

  val createPostEndpoint: Endpoint[String, PostWithoutCreated, Message, Message, Any] =
    jwtAuthEndpoint
      .in("posts")
      .in(postWithoutCreatedBody)
      .post
      .out(messageBody)

  val updatePostEndpoint: Endpoint[String, (String, PostUpdateBody), Message, Message, Any] =
    jwtAuthEndpoint
      .in("posts" / path[String]("postId"))
      .post
      .in(postUpdateBody)
      .out(messageBody)
      .description("update post")

  val deletePostEndpoint: Endpoint[String, String, Message, Message, Any] =
    jwtAuthEndpoint
      .in("posts" / path[String]("postId"))
      .delete
      .out(messageBody)
      .description("delete post")

  val updatePasswordEndpoint: Endpoint[String, NewPassword, Message, Message, Any] =
    jwtAuthEndpoint
      .in("users" / "updatePassword")
      .post
      .in(newPasswordBody)
      .out(messageBody)
      .description("update user password")
}
