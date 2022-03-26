package blog

import java.time.Instant

object Entity {
  final case class Post(postId: String, title: String, content: String, createdAt: Instant)
  final case class Comment(postId: String, content: String, createdAt: Instant)
}
