package gcnyin.blog

import akka.actor.typed.ActorSystem
import gcnyin.blog.database.MongoClient
import gcnyin.blog.repository.PostRepository.{Impl => PostRepositoryImpl}
import gcnyin.blog.repository.UserRepository.{Impl => UserRepositoryImpl}
import gcnyin.blog.repository.{PostRepository, UserRepository}
import zio._

object ZioLayer {
  private val mongoClientLive: URLayer[Has[ActorSystem[Nothing]], Has[MongoClient]] =
    (new MongoClient(_)).toLayer

  type RepositoryEnv = Has[ActorSystem[Nothing]] with Has[MongoClient]

  private val postRepositoryLive: URLayer[RepositoryEnv, Has[PostRepository]] =
    (new PostRepositoryImpl(_, _)).toLayer

  private val userRepositoryLive: URLayer[RepositoryEnv, Has[UserRepository]] =
    (new UserRepositoryImpl(_, _)).toLayer

  type ServiceEnv = Has[UserRepository] with Has[PostRepository] with Has[ActorSystem[Nothing]]

  private val serviceLive: ZLayer[ServiceEnv, Nothing, Has[ServiceLogic]] =
    (new ServiceLogic(_, _, _)).toLayer

  private val controllerLive: URLayer[Has[ServiceLogic], Has[Controller]] =
    (new Controller(_)).toLayer

  def controllerUManaged(actorSystem: ActorSystem[Nothing]): UManaged[Controller] = {
    val actorSystemLayer = ZLayer.succeed(actorSystem)
    val mongoLayer = actorSystemLayer >>> mongoClientLive
    val repositoryEnvLayer = actorSystemLayer ++ mongoLayer
    val userRepositoryLayer = repositoryEnvLayer >>> userRepositoryLive
    val postRepositoryLayer = repositoryEnvLayer >>> postRepositoryLive
    val serviceLayer = actorSystemLayer ++ userRepositoryLayer ++ postRepositoryLayer >>> serviceLive
    val controllerLayer = serviceLayer >>> controllerLive

    controllerLayer.build
      .map(_.get)
  }
}
