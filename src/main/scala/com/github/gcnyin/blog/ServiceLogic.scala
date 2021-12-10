package com.github.gcnyin.blog

import akka.actor.typed.ActorSystem
import cats.data.EitherT
import cats.implicits._
import com.github.gcnyin.blog.Model._
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import sttp.tapir.model.UsernamePassword

import java.time.Instant
import scala.concurrent.{ExecutionContextExecutor, Future}

class ServiceLogic(userRepository: UserRepository, postRepository: PostRepository)(implicit
    val ec: ExecutionContextExecutor,
    system: ActorSystem[_]
) {

  private val passwordEncoder: PasswordEncoder = new BCryptPasswordEncoder()
  private val key = system.settings.config.getString("jwt.key")
  private val jwtComponent: JwtComponent = new JwtComponent(key, Instant.now)

  def getPostById(postId: String): Future[Either[Message, Post]] =
    postRepository.getPostById(postId)

  def createToken(username: String): Future[Either[Message, Token]] =
    Future.successful(Right(Token(jwtComponent.createToken(username))))

  def verifyToken(token: String): Future[Either[Message, UserWithoutPassword]] = {
    for {
      username <- EitherT.fromEither[Future](jwtComponent.parseToken(token))
      up <- EitherT.liftF(userRepository.getUserWithoutPasswordByUsername(username))
      user <- EitherT.fromOption[Future](up, Message(s"username $username not found"))
    } yield user
  }.value

  def verifyUsernamePassword(usernamePassword: UsernamePassword): Future[Either[Message, String]] =
    for {
      user <- userRepository.getUserByUsername(usernamePassword.username)
    } yield verifyPassword(user, usernamePassword)

  def getPostsWithoutContent: Future[Either[Message, Seq[PostWithoutContent]]] =
    postRepository
      .getPostsWithoutContent()
      .map(posts => Right(posts.sortBy(_.created).reverse))

  def savePostWithoutCreated(
      user: UserWithoutPassword,
      post: PostWithoutCreated
  ): Future[Either[Message, Message]] =
    postRepository.savePostWithoutCreated(post)

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
