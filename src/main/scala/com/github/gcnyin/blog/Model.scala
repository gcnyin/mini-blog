package com.github.gcnyin.blog

import reactivemongo.api.bson.Macros.Annotations.{Key, Reader}
import reactivemongo.api.bson.{BSONDocumentHandler, BSONObjectID, BSONReader, Macros}

object Model {
  case class Message(msg: String)

  case class User(username: String, password: String)

  implicit val userHandler: BSONDocumentHandler[User] = Macros.handler[User]

  case class UserWithoutPassword(username: String)

  implicit val userWithoutPasswordHandler: BSONDocumentHandler[UserWithoutPassword] =
    Macros.handler[UserWithoutPassword]

  val idReader: BSONReader[String] = BSONReader.collect[String] { case id @ BSONObjectID(_) =>
    id.asInstanceOf[BSONObjectID].stringify
  }

  case class Post(@Reader(idReader) @Key("_id") id: String, title: String, content: String, created: Long)

  implicit val postHandler: BSONDocumentHandler[Post] = Macros.handler[Post]

  case class PostWithoutContent(@Reader(idReader) @Key("_id") id: String, title: String, created: Long)

  implicit val postWithoutContentHandler: BSONDocumentHandler[PostWithoutContent] =
    Macros.handler[PostWithoutContent]

  case class PostWithoutCreated(title: String, content: String)

  implicit val postWithoutCreatedHandler: BSONDocumentHandler[PostWithoutCreated] =
    Macros.handler[PostWithoutCreated]

  case class Token(token: String)

  implicit val tokenHandler: BSONDocumentHandler[Token] =
    Macros.handler[Token]
}
