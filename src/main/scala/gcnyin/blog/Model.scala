package gcnyin.blog

import reactivemongo.api.bson.Macros.Annotations.{Key, Reader}
import reactivemongo.api.bson.{BSONDocumentHandler, BSONObjectID, BSONReader, Macros}

object Model {
  final case class Message(msg: String)

  final case class User(username: String, password: String)

  implicit val userHandler: BSONDocumentHandler[User] = Macros.handler[User]

  final case class UserWithoutPassword(username: String)

  implicit val userWithoutPasswordHandler: BSONDocumentHandler[UserWithoutPassword] =
    Macros.handler[UserWithoutPassword]

  val idReader: BSONReader[String] = BSONReader.collect[String] { case id @ BSONObjectID(_) =>
    id.asInstanceOf[BSONObjectID].stringify
  }

  final case class Post(@Reader(idReader) @Key("_id") id: String, title: String, content: String, created: Long)

  implicit val postHandler: BSONDocumentHandler[Post] = Macros.handler[Post]

  final case class PostWithoutContent(@Reader(idReader) @Key("_id") id: String, title: String, created: Long)

  implicit val postWithoutContentHandler: BSONDocumentHandler[PostWithoutContent] =
    Macros.handler[PostWithoutContent]

  final case class PostUpdateBody(postId: String, title: String, content: String)

  final case class PostWithoutCreated(title: String, content: String)

  implicit val postWithoutCreatedHandler: BSONDocumentHandler[PostWithoutCreated] =
    Macros.handler[PostWithoutCreated]

  final case class Token(token: String)

  implicit val tokenHandler: BSONDocumentHandler[Token] =
    Macros.handler[Token]
}
