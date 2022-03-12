package blog

object Response {
  final case class Post(postId: String, title: String, content: String)
}
