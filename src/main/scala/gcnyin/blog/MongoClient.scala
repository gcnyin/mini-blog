package gcnyin.blog

import akka.actor.typed.ActorSystem
import com.typesafe.config.Config
import reactivemongo.api.MongoConnection.ParsedURI
import reactivemongo.api.bson.collection.BSONCollection
import reactivemongo.api.{AsyncDriver, DB, MongoConnection}

import scala.concurrent.{ExecutionContext, Future}

class MongoClient(actorSystem: ActorSystem[_]) {
  private implicit val ec: ExecutionContext = actorSystem.executionContext

  private val config: Config =
    actorSystem.settings.config

  private val mongoUri: String =
    config.getString("mongo.uri")

  private val dbName: String =
    config.getString("mongo.database")

  private val driver: AsyncDriver =
    AsyncDriver()

  private val parsedUri: Future[ParsedURI] =
    MongoConnection.fromString(mongoUri)

  private val futureConnection: Future[MongoConnection] =
    parsedUri.flatMap(it => driver.connect(it))

  private val database: Future[DB] =
    futureConnection.flatMap(_.database(dbName))

  private def getCollectionFuture(collection: String): Future[BSONCollection] =
    database.map(_.collection(collection))

  def postsCollectionFuture: Future[BSONCollection] = getCollectionFuture("posts")

  def usersCollectionFuture: Future[BSONCollection] = getCollectionFuture("users")
}
