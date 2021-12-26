package gcnyin.blog.common

object Dto {
  final case class Message(msg: String)

  final case class User(username: String, password: String)

  final case class UserWithoutPassword(username: String)

  final case class Post(id: String, title: String, content: String, created: Long)

  final case class PostWithoutContent(id: String, title: String, created: Long)

  final case class PostUpdateBody(postId: String, title: String, content: String)

  final case class PostWithoutCreated(title: String, content: String)

  final case class Token(token: String)

  final case class NewPassword(password: String)
}
