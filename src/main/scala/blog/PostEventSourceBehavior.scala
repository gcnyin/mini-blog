package blog

import akka.actor.typed.{ActorRef, Behavior}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import java.util.UUID

object PostEventSourceBehavior {
  sealed trait Command
  final case class CreatePostCommand(
      title: String,
      content: String,
      replyTo: ActorRef[StatusReply[Either[Message, Unit]]]
  ) extends Command
  final case class GetPostByPostIdQuery(
      postId: String,
      replyTo: ActorRef[StatusReply[Either[Message, Entity.Post]]]
  ) extends Command
  final case class GetPosts(replyTo: ActorRef[StatusReply[Either[Message, Seq[Entity.Post]]]]) extends Command

  sealed trait Event extends JsonSerializable
  final case class CreatePostEvent(id: String, title: String, content: String) extends Event

  final case class State(posts: Map[String, Entity.Post])

  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("post-persistence-id"),
      emptyState = State(Map.empty),
      commandHandler = (state, cmd) => {
        cmd match {
          case CreatePostCommand(title, content, replyTo) =>
            Effect
              .persist(CreatePostEvent(UUID.randomUUID().toString, title, content))
              .thenReply(replyTo)(_ => StatusReply.success(Right(())))
          case GetPostByPostIdQuery(postId, replyTo) =>
            Effect.reply(replyTo)(
              StatusReply.success(state.posts.get(postId).toRight(Message(s"post not found: $postId")))
            )
          case GetPosts(replyTo) =>
            Effect.reply(replyTo)(StatusReply.success(Right(state.posts.values.toSeq)))
        }
      },
      eventHandler = (state, event) => {
        event match {
          case CreatePostEvent(id, title, content) =>
            state.copy(posts = state.posts.updated(id, Entity.Post(id, title, content)))
        }
      }
    )
}
