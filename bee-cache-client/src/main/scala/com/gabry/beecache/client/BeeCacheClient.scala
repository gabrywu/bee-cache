package com.gabry.beecache.client

import akka.actor.{ActorPath, ActorRef, ActorSystem, Address}
import akka.cluster.Cluster
import akka.cluster.sharding.ClusterSharding
import akka.pattern._
import akka.util.Timeout
import com.gabry.beecache.core.constant.Constants
import com.gabry.beecache.core.extractor.BeeCacheMessageExtractor
import com.gabry.beecache.core.registry.RegistryFactory
import com.gabry.beecache.protocol.BeeCacheData
import com.gabry.beecache.protocol.command.EntityCommand
import com.gabry.beecache.protocol.event.EntityEvent
import com.gabry.beecache.protocol.exception.{BeeCacheException, UnknownBeeCacheException}
import com.typesafe.config.Config
import org.slf4j.LoggerFactory

import java.util.Optional
import java.util.concurrent.TimeUnit
import scala.concurrent.{Await, Future}
import scala.util.Try

/**
 * Created by gabry on 2018/7/2 10:48
 */
class BeeCacheClient(config: Config) extends AbstractBeeCacheClient(config) {

  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = LoggerFactory.getLogger(classOf[BeeCacheClient])
  private val clusterName: String = config.getString("cluster.name")
  private implicit val defaultTimeout: Timeout = Timeout(config.getDuration("client.request-time-out").toMillis, TimeUnit.MILLISECONDS)
  private val shardingRole: String = config.getString("akka.cluster.sharding.role")
  private val numberOfShards = config.getInt("server.number-of-shards")
  private var system: ActorSystem = _
  private var beeCacheRegion: ActorRef = _

  /**
   * 链接server
   */
  override def initialize(): Unit = {
    log.info("Initializing")
    system = ActorSystem(clusterName, config)
    val registry = RegistryFactory.getRegistryOrDefault(config)
    val seeds = try {
      registry.connect()
      registry.getNodesByType("seed").map(node => ActorPath.fromString(node.anchor).address)
    } catch {
      case exception: Exception =>
        log.error("Cannot connect to registry {}", exception.getMessage, exception)
        registry.disConnect()
        Array.empty[Address]
    }

    if (seeds.nonEmpty) {
      val cluster = Cluster(system)
      cluster.joinSeedNodes(seeds)
    }
    beeCacheRegion = ClusterSharding(system).startProxy(
      typeName = Constants.ENTITY_TYPE_NAME
      , role = Optional.of(shardingRole)
      , messageExtractor = BeeCacheMessageExtractor(numberOfShards))
    system.registerOnTermination {
      registry.disConnect()
    }
  }

  /**
   * 断开server链接
   */
  override def destroy(): Unit = {
    val cluster = Cluster(system)
    cluster.leave(cluster.selfAddress)
    system.stop(beeCacheRegion)
    system.terminate()
    log.warn("Destroyed")
  }

  override def get(key: String): Try[BeeCacheData] = Try {
    Await.result(beeCacheRegion ? EntityCommand.Get(key), defaultTimeout.duration).asInstanceOf[BeeCacheData]
  }

  override def set(key: String, value: Option[Any], expireTime: Long): Try[Boolean] = set(BeeCacheData(key, value, expireTime))


  override def set(data: BeeCacheData): Try[Boolean] = Try {
    Await.result(beeCacheRegion ? EntityCommand.Set(data.key, data.value, data.expireTime), defaultTimeout.duration) match {
      case evt: EntityEvent.Updated => evt.key == data.key
      case exception: BeeCacheException => throw exception
      case otherResult => throw UnknownBeeCacheException(s"Get unknown result for set key[$data]: $otherResult")
    }
  }

  override def setExpire(key: String, expireTime: Long): Try[Boolean] = Try {
    Await.result(beeCacheRegion ? EntityCommand.SetExpire(key, expireTime), defaultTimeout.duration) match {
      case _: EntityEvent.Updated => true
      case exception: BeeCacheException => throw exception
      case otherResult => throw UnknownBeeCacheException(s"Get unknown result for set key expire [$key]: $otherResult")
    }
  }

  override def delete(key: String): Try[Boolean] = Try {
    Await.result(beeCacheRegion ? EntityCommand.Delete(key), defaultTimeout.duration) match {
      case _: EntityEvent.Deleted => true
      case exception: BeeCacheException => throw exception
      case otherResult => throw UnknownBeeCacheException(s"Get unknown result for delete key[$key]: $otherResult")
    }
  }

  override def select(key: String): Try[BeeCacheData] = Try {
    Await.result(beeCacheRegion ? EntityCommand.Select(key), defaultTimeout.duration) match {
      case data: BeeCacheData => data
      case exception: BeeCacheException => throw exception
      case otherResult => throw UnknownBeeCacheException(s"Get unknown result for select key[$key]: $otherResult")
    }
  }

  override def asyncGet(key: String): Future[BeeCacheData] = {
    beeCacheRegion ? EntityCommand.Get(key) map (_.asInstanceOf[BeeCacheData])
  }

  override def asyncSet(key: String, value: Option[Any], expireTime: Long): Future[Boolean] = asyncSet(BeeCacheData(key, value, expireTime))

  override def asyncSet(data: BeeCacheData): Future[Boolean] =
    beeCacheRegion ? EntityCommand.Set(data.key, data.value, data.expireTime) map (result => if (result.isInstanceOf[EntityEvent.Updated]) true else false)

  override def asyncSetExpire(key: String, expireTime: Long): Future[Boolean] =
    beeCacheRegion ? EntityCommand.SetExpire(key, expireTime) map (result => if (result.isInstanceOf[EntityEvent.Updated]) true else false)

  override def asyncDelete(key: String): Future[Boolean] =
    beeCacheRegion ? EntityCommand.Delete(key) map (result => if (result.isInstanceOf[EntityEvent.Deleted]) true else false)

  override def asyncSelect(key: String): Future[BeeCacheData] =
    beeCacheRegion ? EntityCommand.Select(key) map (_.asInstanceOf[BeeCacheData])
}
