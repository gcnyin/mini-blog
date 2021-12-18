package gcnyin.blog.frontend

import gcnyin.blog.common.Dto._
import gcnyin.blog.common.TapirEndpoint._
import sttp.client3.Request
import sttp.tapir.DecodeResult
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.tapir.model.UsernamePassword

object HttpClient {
  type GetPostsFunc = Unit => Request[DecodeResult[Either[Message, Seq[PostWithoutContent]]], Any]

  val getPosts: GetPostsFunc = SttpClientInterpreter().toRequest(postsEndpoint, Option.empty)

  type GetPostFunc = String => Request[DecodeResult[Either[Message, Post]], Any]

  val getPost: GetPostFunc = SttpClientInterpreter().toRequest(postEndpoint, Option.empty)

  type CreateTokenFunc = UsernamePassword => Unit => Request[DecodeResult[Either[Message, Token]], Any]

  val createToken: CreateTokenFunc = SttpClientInterpreter().toSecureRequest(createTokenEndpoint, Option.empty)

  type CreatePostFunc = String => PostWithoutCreated => Request[DecodeResult[Either[Message, Message]], Any]

  val createPost: CreatePostFunc = SttpClientInterpreter().toSecureRequest(createPostEndpoint, Option.empty)

  type UpdatePostFunc = String => ((String, PostUpdateBody)) => Request[DecodeResult[Either[Message, Message]], Any]

  val updatePost: UpdatePostFunc = SttpClientInterpreter().toSecureRequest(updatePostEndpoint, Option.empty)

  type UpdatePasswordFunc = String => NewPassword => Request[DecodeResult[Either[Message, Message]], Any]

  val updatePassword: UpdatePasswordFunc = SttpClientInterpreter().toSecureRequest(updatePasswordEndpoint, Option.empty)
}
