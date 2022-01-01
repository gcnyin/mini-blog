package gcnyin.blog.domain.algebra

import gcnyin.blog.domain.entity.User

trait UserRepositoryAlgebra[F[_]] {
  def getUserById(userId: String): F[Option[User]]

  def verifyUsernameAndPassword(username: String, password: String): F[Boolean]
}
