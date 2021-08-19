package com.gabry.beecache.core.server

import akka.actor.{ActorPath, ActorSystem, Address}
import akka.cluster.Cluster
import com.gabry.beecache.core.constant.Constants
import com.gabry.beecache.core.registry.{Node, RegistryFactory}
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}

/**
 * Created by gabry on 2018/7/2 16:48
 * 启动集群种子节点
 */
object BeeCacheSeedServer {
  private val log = LoggerFactory.getLogger(BeeCacheSeedServer.getClass)

  def main(args: Array[String]): Unit = {
    val defaultConfig = ConfigFactory.load()
    val registry = RegistryFactory.getRegistryOrDefault(defaultConfig)
    registry.connect() match {
      case Success(_) =>
        val seeds = registry.getNodesByType(Constants.ROLE_SEED_NAME).map(node => ActorPath.fromString(node.anchor).address)
        val config = ConfigFactory.parseString(s"akka.cluster.seed-nodes=[]")
          .withFallback(ConfigFactory.parseString(s"akka.cluster.roles=[${Constants.ROLE_SEED_NAME}]"))
          .withFallback(defaultConfig.getConfig(Constants.ROLE_SEED_NAME))
          .withFallback(defaultConfig)

        val clusterName = config.getString("cluster.name")
        val system = ActorSystem(clusterName, config)
        val cluster = Cluster(system)

        if (seeds.nonEmpty) {
          log.info(s"Current cluster seed node: ${seeds.mkString(",")}")
          cluster.joinSeedNodes(seeds)
        } else {
          log.warn("Current cluster is empty ,now join self")
          cluster.join(cluster.selfAddress)
        }
        val seedNode = Node("seed", cluster.selfAddress.toString)
        log.info(s"Registry current seed node $seedNode")
        registry.registerNode(seedNode)

        system.registerOnTermination {
          registry.unRegisterNode(seedNode)
          registry.disConnect()
        }
      case Failure(exception) =>
        log.error("Cannot connect to registry {}", exception.getMessage, exception)
        Array.empty[Address]
    }
  }
}
