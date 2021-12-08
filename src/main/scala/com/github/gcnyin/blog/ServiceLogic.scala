package com.github.gcnyin.blog

import com.github.gcnyin.blog.Model._
import reactivemongo.api.bson.document

import scala.concurrent.{ExecutionContextExecutor, Future}

class ServiceLogic(mongoClient: MongoClient)(implicit val ec: ExecutionContextExecutor) {
  def getPostsWithoutContent: Future[Either[Message, Seq[PostWithoutContent]]] =
    for {
      coll <- mongoClient.postsCollectionFuture
      f <- coll.find(document())
        .cursor[PostWithoutContent]()
        .collect[Vector]()
    } yield Right(f.sortBy(_.created).reverse)

  def savePostWithoutCreated(post: PostWithoutCreated): Future[Either[Message, Message]] =
    for {
      coll <- mongoClient.postsCollectionFuture
      _ <- coll.insert.one(document(
        "title" -> post.title,
        "content" -> post.content,
        "created" -> System.currentTimeMillis()
      ))
    } yield Right(Message("Post saved successfully"))
}
