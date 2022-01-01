package gcnyin.blog.domain.service

import gcnyin.blog.domain.vo.Message
import gcnyin.blog.domain.vo.Token

trait AuthService[F[_]] {
  def createToken(username: String): F[Token]

  def getUsernameFromToken(token: Token): F[Either[Message, String]]
}
