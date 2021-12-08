package com.github.gcnyin.blog

import cats.data.EitherT
import com.github.gcnyin.blog.Model._
import reactivemongo.api.bson.{BSONDocumentReader, document}

import scala.concurrent.{ExecutionContextExecutor, Future}
import sttp.tapir.model.UsernamePassword
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

class ServiceLogic(mongoClient: MongoClient, jwtComponent: JwtComponent)(implicit val ec: ExecutionContextExecutor) {
  private val passwordEncoder: PasswordEncoder = new BCryptPasswordEncoder()

  def createToken(username: String): Future[Either[Message, Token]] =
    Future.successful(Right(Token(jwtComponent.createToken(username))))

  def verifyToken(token: String): Future[Either[Message, UserWithoutPassword]] = {
    for {
      username <- EitherT.fromEither[Future](jwtComponent.parseToken(token))
      coll <- EitherT.liftF(mongoClient.usersCollectionFuture)
      user <- EitherT(
        coll
          .find(document("username" -> username))
          .one[UserWithoutPassword]
          .map(optionToEither)
      )
    } yield user
  }.value

  def verifyUsernamePassword(usernamePassword: UsernamePassword): Future[Either[Message, String]] =
    for {
      coll <- mongoClient.usersCollectionFuture
      user <- coll.find(document("username" -> usernamePassword.username)).one[User]
    } yield verifyPassword(user, usernamePassword)

  def getPostsWithoutContent: Future[Either[Message, Seq[PostWithoutContent]]] =
    for {
      coll <- mongoClient.postsCollectionFuture
      f <- coll
        .find(document())
        .cursor[PostWithoutContent]()
        .collect[Vector]()
    } yield Right(f.sortBy(_.created).reverse)

  def savePostWithoutCreated(
      userWithoutPassword: UserWithoutPassword,
      post: PostWithoutCreated
  ): Future[Either[Message, Message]] =
    for {
      coll <- mongoClient.postsCollectionFuture
      _ <- coll.insert.one(
        document(
          "title" -> post.title,
          "content" -> post.content,
          "created" -> System.currentTimeMillis()
        )
      )
    } yield Right(Message("Post saved successfully"))

  private def optionToEither[F](o: Option[F]): Either[Message, F] =
    o match {
      case Some(value) => Right(value)
      case None        => Left(Message("element not found"))
    }

  private def verifyPassword(userOption: Option[User], up: UsernamePassword): Either[Message, String] =
    userOption match {
      case Some(user) =>
        up.password match {
          case Some(rawPassword) =>
            if (passwordEncoder.matches(rawPassword, user.password)) Right(user.username)
            else Left(Message("password doesn't match"))
          case None => Left(Message("password missing"))
        }
      case None => Left(Message(s"user ${up.username} not found"))
    }
}
