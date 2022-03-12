package blog

object Response {
  final case class Post(postId: String, title: String, content: String)
  final case class PostTitle(postId: String, title: String)
}
