package com.gabry.beecache.core.extractor

import akka.cluster.sharding.ShardRegion.HashCodeMessageExtractor
import com.gabry.beecache.protocol.command.EntityCommand

/**
 * Created by gabry on 2018/6/29 13:22
 * 提取实体ID、计算shardId的类
 */
case class BeeCacheMessageExtractor(maxNumberOfShards: Int) extends HashCodeMessageExtractor(maxNumberOfShards) {
  /**
   * Extract the entity id from an incoming `message`. If `null` is returned
   * the message will be `unhandled`, i.e. posted as `Unhandled` messages on the event stream
   */
  override def entityId(message: Any): String = message match {
    case cmd: EntityCommand => cmd.key
    case _ => null
  }
}
