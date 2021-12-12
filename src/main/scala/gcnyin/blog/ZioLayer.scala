package gcnyin.blog

import akka.actor.typed.ActorSystem
import gcnyin.blog.PostRepository.{Impl => PostRepositoryImpl}
import gcnyin.blog.UserRepository.{Impl => UserRepositoryImpl}
import zio._

object ZioLayer {
  private val mongoClientLive: URLayer[Has[ActorSystem[_]], Has[MongoClient]] =
    (new MongoClient(_)).toLayer

  type RepositoryEnv = Has[ActorSystem[_]] with Has[MongoClient]

  private val postRepositoryLive: URLayer[RepositoryEnv, Has[PostRepository]] =
    (new PostRepositoryImpl(_, _)).toLayer

  private val userRepositoryLive: URLayer[RepositoryEnv, Has[UserRepository]] =
    (new UserRepositoryImpl(_, _)).toLayer

  type ServiceEnv = Has[UserRepository] with Has[PostRepository] with Has[ActorSystem[_]]

  private val serviceLive: ZLayer[ServiceEnv, Nothing, Has[ServiceLogic]] =
    (new ServiceLogic(_, _, _)).toLayer

  private val controllerLive: URLayer[Has[ServiceLogic], Has[Controller]] =
    (new Controller(_)).toLayer

  def getController(implicit actorSystem: ActorSystem[_]): Controller = {
    val actorSystemLayer: ULayer[Has[ActorSystem[_]]] = ZLayer.succeed(actorSystem)
    val mongoLayer = actorSystemLayer >>> mongoClientLive
    val repositoryEnvLayer = actorSystemLayer ++ mongoLayer
    val userRepositoryLayer = repositoryEnvLayer >>> userRepositoryLive
    val postRepositoryLayer = repositoryEnvLayer >>> postRepositoryLive
    val serviceLayer = actorSystemLayer ++ userRepositoryLayer ++ postRepositoryLayer >>> serviceLive
    val controllerLayer = serviceLayer >>> controllerLive
    Runtime.default.unsafeRun(controllerLayer.build.useNow).get[Controller]
  }
}
