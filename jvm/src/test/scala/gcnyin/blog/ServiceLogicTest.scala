package gcnyin.blog

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import com.softwaremill.macwire.wire
import gcnyin.blog.Model._
import gcnyin.blog.common.Dto.Message
import gcnyin.blog.repository.{PostRepository, UserRepository}
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import sttp.tapir.model.UsernamePassword

import java.time.Instant
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

class ServiceLogicTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll with MockitoSugar {
  val testKit: ActorTestKit = ActorTestKit()

  implicit val system: ActorSystem[Nothing] = testKit.system
  implicit val context: ExecutionContextExecutor = system.executionContext

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

  it should "verify the token successfully if it's valid" in {
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserWithoutPasswordByUsername("tom"))
      .thenReturn(Future.successful(Option(UserWithoutPassword("tom"))))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]
    val key = system.settings.config.getString("jwt.key")
    val jwtComponent: JwtComponent = new JwtComponent(key, Instant.now)
    val token = jwtComponent.createToken("tom")

    val user = serviceLogic.verifyToken(token)

    val e = Await.result(user, 1.second)
    e should be(Right(UserWithoutPassword("tom")))
  }

  it should "verify the token failed if the username in the token doesn't exist" in {
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserWithoutPasswordByUsername("tom"))
      .thenReturn(Future.successful(Option.empty))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]
    val key = system.settings.config.getString("jwt.key")
    val jwtComponent: JwtComponent = new JwtComponent(key, Instant.now)
    val token = jwtComponent.createToken("tom")

    val user = serviceLogic.verifyToken(token)

    val e = Await.result(user, 1.second)
    e should be(Left(Message("username tom not found")))
  }

  it should "verify the username and password successfully if the password is correct" in {
    val encoder = new BCryptPasswordEncoder()
    val rawPassword = "123456"
    val password = encoder.encode(rawPassword)
    val username = "tom"
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserByUsername("tom"))
      .thenReturn(Future.successful(Option(User(username, password))))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]

    val up = UsernamePassword(username, Option(rawPassword))
    val f = serviceLogic.verifyUsernamePassword(up)

    val e = Await.result(f, 1.second)
    e should be(Right(username))
  }

  it should "verify the username and password successfully if the user doesn't exist" in {
    val encoder = new BCryptPasswordEncoder()
    val rawPassword = "123456"
    val password = encoder.encode(rawPassword)
    val username = "tom"
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserByUsername("tom"))
      .thenReturn(Future.successful(Option.empty))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]

    val up = UsernamePassword(username, Option(rawPassword))
    val f = serviceLogic.verifyUsernamePassword(up)

    val e = Await.result(f, 1.second)
    e should be(Left(Message("user tom not found")))
  }

  it should "verify the username and password failed if the password is wrong" in {
    val encoder = new BCryptPasswordEncoder()
    val rawPassword = "123456"
    val password = encoder.encode(rawPassword)
    val username = "tom"
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserByUsername("tom"))
      .thenReturn(Future.successful(Option(User(username, password))))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]
    val anotherPassword = "111111"

    val up = UsernamePassword(username, Option(anotherPassword))
    val f = serviceLogic.verifyUsernamePassword(up)

    val e = Await.result(f, 1.second)
    e should be(Left(Message("username and password do not match")))
  }

  it should "verify the username and password failed if the input password is empty" in {
    val encoder = new BCryptPasswordEncoder()
    val rawPassword = "123456"
    val password = encoder.encode(rawPassword)
    val username = "tom"
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserByUsername("tom"))
      .thenReturn(Future.successful(Option(User(username, password))))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]

    val up = UsernamePassword(username, Option.empty)
    val f = serviceLogic.verifyUsernamePassword(up)

    val e = Await.result(f, 1.second)
    e should be(Left(Message("password missing")))
  }
}
