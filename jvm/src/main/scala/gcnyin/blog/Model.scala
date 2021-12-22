package gcnyin.blog

import gcnyin.blog.common.Dto._
import reactivemongo.api.bson.Macros.Annotations.{Key, Reader}
import reactivemongo.api.bson.{BSONDocumentHandler, BSONObjectID, BSONReader, Macros}

object Model {
  implicit val userHandler: BSONDocumentHandler[User] = Macros.handler[User]

  implicit val userWithoutPasswordHandler: BSONDocumentHandler[UserWithoutPassword] =
    Macros.handler[UserWithoutPassword]

  val idReader: BSONReader[String] = BSONReader.collect[String] { case id @ BSONObjectID(_) =>
    id.asInstanceOf[BSONObjectID].stringify
  }

  final case class PostPo(@Reader(idReader) @Key("_id") id: String, title: String, content: String, created: Long)

  implicit val postHandler: BSONDocumentHandler[PostPo] = Macros.handler[PostPo]

  final case class PostWithoutContentPo(@Reader(idReader) @Key("_id") id: String, title: String, created: Long)

  implicit val postWithoutContentHandler: BSONDocumentHandler[PostWithoutContentPo] =
    Macros.handler[PostWithoutContentPo]

  implicit val postWithoutCreatedHandler: BSONDocumentHandler[PostWithoutCreated] =
    Macros.handler[PostWithoutCreated]

  implicit val tokenHandler: BSONDocumentHandler[Token] =
    Macros.handler[Token]
}
