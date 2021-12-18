package gcnyin.blog.repository

import akka.actor.typed.ActorSystem
import cats.data.EitherT
import gcnyin.blog.Model._
import gcnyin.blog.common.Dto.{Message, PostUpdateBody}
import gcnyin.blog.database.MongoClient
import reactivemongo.api.bson.{BSONObjectID, document}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContextExecutor, Future}

trait PostRepository {
  def getPostsWithoutContent: Future[Seq[PostWithoutContent]]

  def getPostById(postId: String): Future[Either[Message, Post]]

  def savePostWithoutCreated(post: PostWithoutCreated): Future[Either[Message, Message]]

  def updatePost(postId: String, post: PostUpdateBody): Future[Either[Message, Message]]

  def deletePost(postId: String): Future[Either[Message, Message]]
}

object PostRepository {
  class Impl(actorSystem: ActorSystem[Nothing], mongoClient: MongoClient) extends PostRepository {
    private implicit val ec: ExecutionContextExecutor = actorSystem.executionContext

    private val right: Either[Message, Message] =
      Right(Message("updated successfully"))

    override def getPostsWithoutContent: Future[Seq[PostWithoutContent]] =
      for {
        coll <- mongoClient.postsCollectionFuture
        f <- coll
          .find(document())
          .cursor[PostWithoutContent]()
          .collect[Vector]()
      } yield f

    override def getPostById(postId: String): Future[Either[Message, Post]] = {
      for {
        coll <- EitherT.liftF(mongoClient.postsCollectionFuture)
        id <- EitherT.fromEither[Future](
          BSONObjectID
            .parse(postId)
            .toOption
            .toRight(Message(s"invalid post id: $postId"))
        )
        post <- EitherT(
          coll
            .find(document("_id" -> id))
            .one[Post]
            .map(_.toRight(Message(s"post $postId not found")))
        )
      } yield post
    }.value

    override def savePostWithoutCreated(post: PostWithoutCreated): Future[Either[Message, Message]] =
      for {
        coll <- mongoClient.postsCollectionFuture
        wr: WriteResult <- coll.insert.one(
          document(
            "title" -> post.title,
            "content" -> post.content,
            "created" -> System.currentTimeMillis()
          )
        )
      } yield wr.writeConcernError.fold(right) { it => Left(Message(it.errmsg)) }

    override def updatePost(postId: String, post: PostUpdateBody): Future[Either[Message, Message]] = {
      for {
        coll <- EitherT.liftF(mongoClient.postsCollectionFuture)
        id <- EitherT.fromEither[Future](
          BSONObjectID
            .parse(postId)
            .toOption
            .toRight(Message(s"invalid post id: $postId"))
        )
        msg <- EitherT(
          coll.update
            .one(
              document("_id" -> id),
              document("$set" -> document("title" -> post.title, "content" -> post.content))
            )
            .map(wr => wr.writeConcernError.fold(right) { it => Left(Message(it.errmsg)) })
        )
      } yield msg
    }.value

    override def deletePost(postId: String): Future[Either[Message, Message]] = {
      for {
        coll <- EitherT.liftF(mongoClient.postsCollectionFuture)
        id <- EitherT.fromEither[Future](
          BSONObjectID
            .parse(postId)
            .toOption
            .toRight(Message(s"invalid post id: $postId"))
        )
        msg <- EitherT(
          coll.delete
            .one(
              document("_id" -> id)
            )
            .map(wr => wr.writeConcernError.fold(right) { it => Left(Message(it.errmsg)) })
        )
      } yield msg
    }.value
  }
}
