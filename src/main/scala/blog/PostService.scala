package blog

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout

import java.time.Instant
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class PostService(postEventSourceActorRef: ActorRef[PostEventSourceBehavior.Command])(implicit
    ec: ExecutionContext,
    system: ActorSystem[_]
) {
  private implicit val timeout: Timeout = 5.seconds

  def createPost(title: String, content: String): Future[Either[Message, Unit]] =
    postEventSourceActorRef
      .ask(replyTo => PostEventSourceBehavior.CreatePostCommand(title, content, Instant.now, replyTo))

  def getPostByPostId(postId: String): Future[Either[Message, Response.Post]] =
    postEventSourceActorRef
      .ask(replyTo => PostEventSourceBehavior.GetPostByPostIdQuery(postId, replyTo))
      .map(_.map(p => Response.Post(p.postId, p.title, p.content)))

  def getPosts: Future[Either[Message, Seq[Response.PostTitle]]] =
    postEventSourceActorRef
      .ask(replyTo => PostEventSourceBehavior.GetPosts(replyTo))
      .map(_.map(l => l.map(p => Response.PostTitle(p.postId, p.title))))

  def deletePost(postId: String): Future[Either[Message, Unit]] =
    postEventSourceActorRef
      .ask(replyTo => PostEventSourceBehavior.DeletePost(postId, replyTo))
}
