package com.github.gcnyin.blog

import com.github.gcnyin.blog.Model._
import reactivemongo.api.bson.document

import scala.concurrent.{ExecutionContextExecutor, Future}

trait UserRepository {
  def getUserByUsername(username: String): Future[Option[User]]

  def getUserWithoutPasswordByUsername(username: String): Future[Option[UserWithoutPassword]]
}

object UserRepository {
  class Impl(mongoClient: MongoClient)(implicit val ec: ExecutionContextExecutor) extends UserRepository {

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
