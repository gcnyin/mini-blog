package blog

object Request {
  final case class CreatePost(title: String, content: String)
}
