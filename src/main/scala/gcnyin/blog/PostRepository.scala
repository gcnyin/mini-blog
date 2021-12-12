package gcnyin.blog

import cats.data.EitherT
import Model.{Message, Post, PostUpdateBody}
import gcnyin.blog.Model.{Message, Post, PostUpdateBody}
import reactivemongo.api.bson.{BSONObjectID, document}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.{ExecutionContextExecutor, Future}

trait PostRepository {
  def getPostsWithoutContent: Future[Seq[Model.PostWithoutContent]]

  def getPostById(postId: String): Future[Either[Message, Post]]

  def savePostWithoutCreated(post: Model.PostWithoutCreated): Future[Either[Message, Message]]

  def updatePost(postId: String, post: PostUpdateBody): Future[Either[Message, Message]]
}

object PostRepository {
  class Impl(mongoClient: MongoClient)(implicit val ec: ExecutionContextExecutor) extends PostRepository {
    private val right: Either[Message, Message] =
      Right(Message("Post saved successfully"))

    override def getPostsWithoutContent: Future[Seq[Model.PostWithoutContent]] =
      for {
        coll <- mongoClient.postsCollectionFuture
        f <- coll
          .find(document())
          .cursor[Model.PostWithoutContent]()
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

    override def savePostWithoutCreated(post: Model.PostWithoutCreated): Future[Either[Message, Message]] =
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
  }
}
