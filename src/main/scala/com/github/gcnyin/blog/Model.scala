package com.github.gcnyin.blog

import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

object Model {
  case class Post(title: String, content: String, created: Long)

  implicit val postHandler: BSONDocumentHandler[Post] =
    Macros.handler[Post]

  case class PostWithoutContent(title: String, created: Long)

  implicit val postWithoutContentHandler: BSONDocumentHandler[PostWithoutContent] =
    Macros.handler[PostWithoutContent]

  case class PostWithoutCreated(title: String, content: String)

  implicit val postWithoutCreatedHandler: BSONDocumentHandler[PostWithoutCreated] =
    Macros.handler[PostWithoutCreated]
}

