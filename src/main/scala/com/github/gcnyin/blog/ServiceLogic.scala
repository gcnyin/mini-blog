package com.github.gcnyin.blog

import cats.implicits._
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
          .map(_.toRight(Message(s"username $username not found")))
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

  private def verifyPassword(
      ou: Option[User],
      up: UsernamePassword
  ): Either[Message, String] =
    for {
      user <- ou.toRight(Message(s"user ${up.username} not found"))
      rawPassword <- up.password.toRight(Message("password missing"))
      username <-
        if (passwordEncoder.matches(rawPassword, user.password)) Right(user.username)
        else Left(Message("password doesn't match"))
    } yield username
}
