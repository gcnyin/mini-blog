package blog

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.util.Timeout

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class PostService(postEventSourceActorRef: ActorRef[PostEventSourceBehavior.Command])(implicit
    ec: ExecutionContext,
    system: ActorSystem[_]
) {
  private implicit val timeout: Timeout = 5.seconds

  def createPost(title: String, content: String): Future[Either[Message, Unit]] =
    postEventSourceActorRef
      .ask(replyTo => PostEventSourceBehavior.CreatePostCommand(title, content, replyTo))
      .map(_.getValue)

  def getPostByPostId(postId: String): Future[Either[Message, Response.Post]] = {
    postEventSourceActorRef
      .ask(replyTo => PostEventSourceBehavior.GetPostByPostIdQuery(postId, replyTo))
      .map(_.getValue.map(p => Response.Post(p.id, p.title, p.content)))
  }
}
