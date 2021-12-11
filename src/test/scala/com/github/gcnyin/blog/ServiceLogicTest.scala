package com.github.gcnyin.blog

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import com.github.gcnyin.blog.Model.User
import com.softwaremill.macwire.wire
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import sttp.tapir.model.UsernamePassword

import java.time.Instant
import scala.concurrent.{ExecutionContextExecutor, Future}

class ServiceLogicTest extends AnyFlatSpec with Matchers with BeforeAndAfterAll with MockitoSugar {
  val testKit: ActorTestKit = ActorTestKit()

  implicit val system: ActorSystem[Nothing] = testKit.system
  implicit val context: ExecutionContextExecutor = system.executionContext

  override protected def afterAll(): Unit = testKit.shutdownTestKit()

  it should "verify the token successfully if it's valid" in {
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserByUsername("tom"))
      .thenReturn(Future.successful(Option(Model.User("tom", "123456"))))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]
    val key = system.settings.config.getString("jwt.key")
    val jwtComponent: JwtComponent = new JwtComponent(key, Instant.now)
    val token = jwtComponent.createToken("tom")

    val user = serviceLogic.verifyToken(token)

    user.map { e =>
      e.isRight should be(true)
      e.map { u =>
        u.username should be("tome")
      }
    }
  }

  it should "verify the token failed if the username in the token doesn't exist" in {
    lazy val userRepository = mock[UserRepository]
    when(userRepository.getUserByUsername("tom"))
      .thenReturn(Future.successful(Option.empty))
    lazy val postRepository = mock[PostRepository]
    lazy val serviceLogic: ServiceLogic = wire[ServiceLogic]
    val key = system.settings.config.getString("jwt.key")
    val jwtComponent: JwtComponent = new JwtComponent(key, Instant.now)
    val token = jwtComponent.createToken("tom")

    val user = serviceLogic.verifyToken(token)

    user.map { e =>
      e.isLeft should be(true)
    }
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

    val up = UsernamePassword(username, Option(password))
    val f = serviceLogic.verifyUsernamePassword(up)

    f.map { e =>
      e.isRight should be(true)
      e.map { p =>
        encoder.matches(rawPassword, p) should be(true)
      }
    }
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

    f.map { e =>
      e.isLeft should be(true)
    }
  }
}
