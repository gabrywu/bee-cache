package com.gabry.beecache.core.server

import akka.actor.{ActorPath, ActorSystem, Address}
import akka.cluster.Cluster
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
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

object BeeCacheServerInMem {
  private val log = LoggerFactory.getLogger(BeeCacheServerInMem.getClass)

  def main(args: Array[String]): Unit = {
    val port = args.headOption.map(_.toInt).getOrElse(0)

    val defaultConfig = ConfigFactory.load()
    val registry = RegistryFactory.getRegistryOrDefault(defaultConfig)
    registry.connect() match {
      case Success(_) =>
        val seeds = registry.getNodesByType(Constants.ROLE_SEED_NAME).map(node => ActorPath.fromString(node.anchor).address)
        if (seeds.nonEmpty) {
          val config = ConfigFactory.parseString(s"akka.remote.artery.canonical.port=$port")
            .withFallback(defaultConfig.getConfig("server"))
            .withFallback(ConfigFactory.parseString(s"akka.cluster.seed-nodes=[]"))
            .withFallback(defaultConfig)

          val clusterName = config.getString("cluster.name")
          val system = ActorSystem(clusterName, config)

          system.registerOnTermination {
            registry.disConnect()
          }

          val cluster = Cluster(system)
          cluster.joinSeedNodes(seeds)

          implicit val timeout: Timeout = Timeout(config.getDuration("server.db-store-resolve-timeout").toMillis, TimeUnit.MILLISECONDS)
          implicit val executionContext: ExecutionContextExecutor = system.dispatcher

          val numberOfShards = config.getInt("server.number-of-shards")
          val shardRegion = ClusterSharding(system).start(
            typeName = Constants.ENTITY_TYPE_NAME,
            entityProps = BeeCacheActor.props,
            settings = ClusterShardingSettings(system),
            messageExtractor = BeeCacheMessageExtractor(numberOfShards))
          log.info(s"ShardRegion started at $shardRegion with shard number $numberOfShards")
        }

        log.info("BeeCache server started")
      case Failure(exception) =>
        log.error("Cannot connect to registry {}", exception.getMessage, exception)
        Array.empty[Address]
    }


  }

}
