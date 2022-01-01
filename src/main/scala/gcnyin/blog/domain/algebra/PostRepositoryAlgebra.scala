package gcnyin.blog.domain.algebra

import gcnyin.blog.domain.entity.Post
import gcnyin.blog.domain.vo.PostToSave

trait PostRepositoryAlgebra[F[_]] {
  def getPosts: F[Seq[Post]]

  def getPostById(postId: String): F[Option[Post]]

  def savePost(post: PostToSave): F[Post]
}
