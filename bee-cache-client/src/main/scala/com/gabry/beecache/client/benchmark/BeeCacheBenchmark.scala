package com.gabry.beecache.client.benchmark

import akka.actor.{ActorPath, ActorSystem, Props}
import akka.cluster.Cluster
import akka.cluster.sharding.ClusterSharding
import com.gabry.beecache.client.BeeCacheClient
import com.gabry.beecache.core.constant.Constants
import com.gabry.beecache.core.extractor.BeeCacheMessageExtractor
import com.gabry.beecache.core.registry.RegistryFactory
import com.gabry.beecache.protocol.BeeCacheData
import com.gabry.beecache.protocol.command.EntityCommand
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory

import java.util.Optional
import scala.concurrent.duration.DurationInt
import scala.util.Random

/**
 * Created by gabry on 2018/7/3 14:20
 */
object BeeCacheBenchmark {
  private val log = LoggerFactory.getLogger(BeeCacheBenchmark.getClass)

  def main(args: Array[String]): Unit = {
    val parallel = args.headOption.map(_.toInt).getOrElse(10)
    val msgNumberPerParallel = if (args.length > 1) args(1).toInt else 10000
    val config = ConfigFactory.load()
    val clusterName: String = config.getString("clusterNode.cluster-name")
    val shardingRole: String = config.getString("akka.cluster.sharding.role")
    val numberOfShards = config.getInt("server.number-of-shards")
    val system = ActorSystem(clusterName, config)
    val registry = RegistryFactory.getRegistryOrDefault(config)
    try {
      registry.connect()
      val seeds = registry.getNodesByType("seed").map(node => ActorPath.fromString(node.anchor).address).toList
      if (seeds.nonEmpty) {
        val cluster = Cluster(system)
        cluster.joinSeedNodes(seeds)
      }
      val data = putData(config)
      val beeCacheRegion = ClusterSharding(system).startProxy(
        typeName = Constants.ENTITY_TYPE_NAME,
        role = Optional.of(shardingRole),
        messageExtractor = BeeCacheMessageExtractor(numberOfShards))

      beeCacheRegion ! EntityCommand.Get(data.key)

      Thread.sleep(3 * 1000)
      val mark = system.actorOf(Props(new BenchmarkActor(beeCacheRegion, config, parallel, msgNumberPerParallel)))
    } catch {
      case exception: Exception =>
        log.error(s"Cannot connect to Registry: $exception")
    } finally {
      registry.disConnect()
    }
  }

  private def putData(config: Config): BeeCacheData = {
    val client = new BeeCacheClient(config)
    client.initialize()
    val rand = new Random()
    val key = rand.nextInt(1024).toString
    val value = s"this is value ${rand.nextInt(1024)}"
    val data = BeeCacheData(key, Some(value), 3.hour.toMillis)
    client.set(data).foreach(println)
    client.destroy()
    data
  }
}
