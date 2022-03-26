package blog

import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

import java.time.Instant
import java.util.UUID

object PostEventSourceBehavior {
  sealed trait Command
  final case class CreatePostCommand(
      title: String,
      content: String,
      createdAt: Instant,
      replyTo: ActorRef[Either[Message, Unit]]
  ) extends Command
  final case class GetPostByPostIdQuery(
      postId: String,
      replyTo: ActorRef[Either[Message, Entity.Post]]
  ) extends Command
  final case class GetPosts(replyTo: ActorRef[Either[Message, Seq[Entity.Post]]]) extends Command
  final case class DeletePost(postId: String, replyTo: ActorRef[Either[Message, Unit]]) extends Command

  sealed trait Event extends JsonSerializable
  final case class CreatePostEvent(postId: String, title: String, content: String, createdAt: Instant) extends Event
  final case class DeletePostEvent(postId: String) extends Event

  final case class State(posts: Map[String, Entity.Post])

  def apply(): Behavior[Command] =
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("post-persistence-id"),
      emptyState = State(Map.empty),
      commandHandler = (state, cmd) => {
        cmd match {
          case CreatePostCommand(title, content, createdAt, replyTo) =>
            Effect
              .persist(CreatePostEvent(UUID.randomUUID().toString, title, content, createdAt))
              .thenReply(replyTo)(_ => Right(()))
          case GetPostByPostIdQuery(postId, replyTo) =>
            Effect.reply(replyTo)(state.posts.get(postId).toRight(Message(s"post not found: $postId")))
          case GetPosts(replyTo) =>
            Effect.reply(replyTo)(Right(state.posts.values.toSeq))
          case DeletePost(postId, replyTo) =>
            Effect
              .persist(DeletePostEvent(postId = postId))
              .thenReply(replyTo)(_ => Right(()))
        }
      },
      eventHandler = (state, event) => {
        event match {
          case CreatePostEvent(id, title, content, createdAt) =>
            state.copy(posts = state.posts.updated(id, Entity.Post(id, title, content, createdAt)))
          case DeletePostEvent(postId) =>
            state.copy(posts = state.posts.removed(postId))
        }
      }
    )
}
