package gcnyin.blog.repository

import akka.actor.typed.ActorSystem
import cats.data.EitherT
import gcnyin.blog.Model._
import gcnyin.blog.database.MongoClient
import reactivemongo.api.bson.document

import scala.concurrent.{ExecutionContextExecutor, Future}

trait UserRepository {
  def getUserByUsername(username: String): Future[Option[User]]

  def getUserWithoutPasswordByUsername(username: String): Future[Option[UserWithoutPassword]]

  def updateUserPassword(username: String, password: String): Future[Either[Message, Message]]
}

object UserRepository {
  class Impl(actorSystem: ActorSystem[Nothing], mongoClient: MongoClient) extends UserRepository {
    private val right = Message("User saved successfully")

    private implicit val ec: ExecutionContextExecutor = actorSystem.executionContext

    override def getUserByUsername(username: String): Future[Option[User]] =
      for {
        coll <- mongoClient.usersCollectionFuture
        user <- coll.find(document("username" -> username)).one[User]
      } yield user

    override def getUserWithoutPasswordByUsername(username: String): Future[Option[UserWithoutPassword]] =
      for {
        coll <- mongoClient.usersCollectionFuture
        user <- coll.find(document("username" -> username)).one[UserWithoutPassword]
      } yield user

    override def updateUserPassword(username: String, password: String): Future[Either[Message, Message]] = {
      for {
        coll <- EitherT.liftF(mongoClient.usersCollectionFuture)
        wr <- EitherT.liftF(
          coll.update
            .one(
              document("username" -> username),
              document("$set" -> document("password" -> password))
            )
        )
        e <- EitherT.fromEither[Future](
          wr.writeConcernError
            .map(it => Message(it.errmsg))
            .toLeft(right)
        )
      } yield e
    }.value
  }
}
