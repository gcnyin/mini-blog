package com.github.gcnyin.blog

import com.github.gcnyin.blog.Model._
import io.circe.generic.auto._
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe.jsonBody
import sttp.tapir.model.UsernamePassword
import reactivemongo.api.bson.BSONString

object TapirEndpoint {
  private val messageBody: EndpointIO.Body[String, Message] =
    jsonBody[Message]
      .description("Something happen")
      .example(Message("Post saved successfully"))

  private val postWithoutContentSeqBody: EndpointIO.Body[String, Seq[PostWithoutContent]] =
    jsonBody[Seq[PostWithoutContent]]
      .description("Post without content")
      .example(Seq(PostWithoutContent("title", System.currentTimeMillis())))

  private val postWithoutCreatedBody: EndpointIO.Body[String, PostWithoutCreated] =
    jsonBody[PostWithoutCreated]
      .description("Post that has not been saved")

  private val token: EndpointIO.Body[String, Token] =
    jsonBody[Token]
      .description("Json Web Token")

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
      .out(token)

  val postsEndpoint: Endpoint[Unit, Unit, Message, Seq[PostWithoutContent], Any] =
    basicEndpoint
      .in("posts")
      .description("posts")
      .get
      .out(postWithoutContentSeqBody)

  val createPostEndpoint: Endpoint[String, PostWithoutCreated, Message, Message, Any] =
    jwtAuthEndpoint
      .in("posts")
      .in(postWithoutCreatedBody)
      .post
      .out(messageBody)
}
