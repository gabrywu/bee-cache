package com.gabry.beecache.core.server

import akka.actor.{ActorIdentity, ActorPath, ActorSystem, Identify, RootActorPath}
import akka.cluster.Cluster
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.pattern._
import akka.persistence.journal.leveldb.SharedLeveldbJournal
import akka.util.Timeout
import com.gabry.beecache.core.actor.BeeCacheActor
import com.gabry.beecache.core.constant.Constants
import com.gabry.beecache.core.extractor.BeeCacheMessageExtractor
import com.gabry.beecache.core.registry.RegistryFactory
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

/**
 * Created by gabry on 2018/6/27 15:30
 * 启动缓存服务器接待
 */
object BeeCacheServer {
  private val log = LoggerFactory.getLogger(BeeCacheServer.getClass)

  def main(args: Array[String]): Unit = {
    val port = args.headOption.map(_.toInt).getOrElse(0)

    val defaultConfig = ConfigFactory.load()
    val registry = RegistryFactory.getRegistryOrDefault(defaultConfig)
    var system: Option[ActorSystem] = None
    try {
      registry.connect()
      val seeds = registry.getNodesByType(Constants.ROLE_SEED_NAME).map(node => ActorPath.fromString(node.anchor).address).toList
      if (seeds.nonEmpty) {

        val config = ConfigFactory.parseString(s"akka.remote.artery.canonical.port=$port")
          .withFallback(defaultConfig.getConfig("server"))
          .withFallback(ConfigFactory.parseString(s"akka.cluster.seed-nodes=[]"))
          .withFallback(defaultConfig)

        val clusterName = config.getString(Constants.CLUSTER_NAME_PATH_OF_CONFIG)
        system = Some(ActorSystem(clusterName, config))

        system.get.registerOnTermination {
          registry.disConnect()
        }

        val cluster = Cluster(system.get)
        cluster.joinSeedNodes(seeds)

        implicit val timeout: Timeout = Timeout(config.getDuration("server.db-store-resolve-timeout").toMillis, TimeUnit.MILLISECONDS)
        implicit val executionContext: ExecutionContextExecutor = system.get.dispatcher

        val stores = registry.getNodesByType(Constants.ROLE_SHARED_STORE_NAME).map(node => ActorPath.fromString(node.anchor).address).toList
        log.info(s"Current cluster store node: ${stores.mkString(",")}")

        if (stores.nonEmpty) {
          system.get.actorSelection(RootActorPath(stores.head) / "system" / "store") ? Identify(None) onComplete {
            case Success(ActorIdentity(_, Some(sharedJournalStore))) =>
              log.info(s"Set current store: $sharedJournalStore")
              SharedLeveldbJournal.setStore(sharedJournalStore, system.get)
              val numberOfShards = system.get.settings.config.getInt("server.number-of-shards")
              val shardRegion = ClusterSharding(system.get).start(
                typeName = Constants.ENTITY_TYPE_NAME,
                entityProps = BeeCacheActor.props,
                settings = ClusterShardingSettings(system.get),
                messageExtractor = BeeCacheMessageExtractor(numberOfShards))
              log.info(s"ShardRegion started at $shardRegion with shard number $numberOfShards")
            case Success(ActorIdentity(_, None)) =>
              log.error(s"Cannot resolve shared journal store")
              system.get.terminate()
            case Failure(exception) =>
              log.error(s"Cannot resolve shared journal store: $exception", exception)
              system.get.terminate()
          }
        } else {
          log.error("Store actor not started,you must start it first")
          system.get.terminate()
        }
      } else {
        log.error("Cannot find seed node, you must start it first")
      }
      log.info("BeeCache server started")
    } catch {
      case exception: Exception =>
        log.error("Cannot connect to registry", exception)
        system.foreach(_.terminate())
    }
  }
}
