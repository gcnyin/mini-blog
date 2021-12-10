package com.github.gcnyin.blog

import cats.data.EitherT
import reactivemongo.api.bson.{BSONObjectID, document}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContextExecutor, Future}

trait PostRepository {
  def getPostsWithoutContent(): Future[Seq[Model.PostWithoutContent]]

  def getPostById(postId: String): Future[Either[Model.Message, Model.Post]]

  def savePostWithoutCreated(post: Model.PostWithoutCreated): Future[Either[Model.Message, Model.Message]]
}

object PostRepository {
  class Impl(mongoClient: MongoClient)(implicit val ec: ExecutionContextExecutor) extends PostRepository {
    private val right: Either[Model.Message, Model.Message] =
      Right(Model.Message("Post saved successfully"))

    override def getPostsWithoutContent(): Future[Seq[Model.PostWithoutContent]] =
      for {
        coll <- mongoClient.postsCollectionFuture
        f <- coll
          .find(document())
          .cursor[Model.PostWithoutContent]()
          .collect[Vector]()
      } yield f

    override def getPostById(postId: String): Future[Either[Model.Message, Model.Post]] = {
      for {
        coll <- EitherT.liftF(mongoClient.postsCollectionFuture)
        id <- EitherT.fromEither[Future](
          BSONObjectID
            .parse(postId)
            .toOption
            .toRight(Model.Message(s"invalid post id: $postId"))
        )
        post <- EitherT(
          coll
            .find(document("_id" -> id))
            .one[Model.Post]
            .map(_.toRight(Model.Message(s"post $postId not found")))
        )
      } yield post
    }.value

    override def savePostWithoutCreated(post: Model.PostWithoutCreated): Future[Either[Model.Message, Model.Message]] =
      for {
        coll <- mongoClient.postsCollectionFuture
        wr: WriteResult <- coll.insert.one(
          document(
            "title" -> post.title,
            "content" -> post.content,
            "created" -> System.currentTimeMillis()
          )
        )
      } yield wr.writeConcernError.fold(right) { it => Left(Model.Message(it.errmsg)) }
  }
}
