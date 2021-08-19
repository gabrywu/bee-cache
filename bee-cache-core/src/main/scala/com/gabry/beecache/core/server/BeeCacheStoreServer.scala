package com.gabry.beecache.core.server

import akka.actor.{ActorPath, ActorSystem, Address, ExtendedActorSystem, Props}
import akka.cluster.Cluster
import akka.persistence.journal.leveldb.SharedLeveldbStore
import com.gabry.beecache.core.constant.Constants
import com.gabry.beecache.core.registry.{Node, RegistryFactory}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

/**
 * Created by gabry on 2018/7/3 10:41
 * 启动shared journal节点
 */
object BeeCacheStoreServer {
  private val log = LoggerFactory.getLogger(BeeCacheStoreServer.getClass)

  def main(args: Array[String]): Unit = {
    val port = args.headOption.map(_.toInt).getOrElse(0)

    val defaultConfig = ConfigFactory.load()
    val registry = RegistryFactory.getRegistryOrDefault(defaultConfig)
    registry.connect() match {
      case Success(_) =>
        val seeds = registry.getNodesByType(Constants.ROLE_SEED_NAME)
          .map(node => ActorPath.fromString(node.anchor).address)
        if (seeds.nonEmpty) {

          val config = ConfigFactory.parseString(s"akka.remote.artery.canonical.port=$port")
            .withFallback(ConfigFactory.parseString(s"akka.cluster.seed-nodes=[]"))
            .withFallback(defaultConfig.getConfig(Constants.ROLE_SHARED_STORE_NAME))
            .withFallback(defaultConfig)

          val clusterName = config.getString("cluster.name")
          val system = ActorSystem(clusterName, config)
          val cluster = Cluster(system)
          cluster.joinSeedNodes(seeds)

          val levelDbStore = system.asInstanceOf[ExtendedActorSystem]
            .systemActorOf(Props[SharedLeveldbStore], Constants.ROLE_SHARED_STORE_NAME)

          log.info(s"level db store started at ${levelDbStore}")
          val storeNode = Node(Constants.ROLE_SHARED_STORE_NAME, levelDbStore.path
            .toStringWithAddress(cluster.selfAddress))

          log.info(s"Register store node: $storeNode")
          registry.registerNode(storeNode)
          system.registerOnTermination {
            registry.unRegisterNode(storeNode)
            registry.disConnect()
          }
        } else {
          log.error("Cannot find seed node, you must start it first")
          registry.disConnect()
        }
        log.info("BeeCache Persistence Server started")
      case Failure(exception) =>
        log.error("Cannot connect to registry {}", exception.getMessage, exception)
        Array.empty[Address]
    }
  }
}
