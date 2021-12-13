package gcnyin.blog

import akka.actor.typed.ActorSystem
import gcnyin.blog.Model._
import reactivemongo.api.bson.document

import scala.concurrent.{ExecutionContextExecutor, Future}

trait UserRepository {
  def getUserByUsername(username: String): Future[Option[User]]

  def getUserWithoutPasswordByUsername(username: String): Future[Option[UserWithoutPassword]]
}

object UserRepository {
  class Impl(actorSystem: ActorSystem[Nothing], mongoClient: MongoClient) extends UserRepository {
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
  }
}
