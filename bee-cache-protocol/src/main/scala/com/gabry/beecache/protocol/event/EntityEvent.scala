package com.gabry.beecache.protocol.event

import com.gabry.beecache.protocol.exception.BeeCacheException
import com.gabry.beecache.protocol.{BeeCacheData, EntityMessage}

/**
 * Created by gabry on 2018/6/28 18:05
 * 与实体操作相关的事件
 */
sealed trait EntityEvent extends EntityMessage

object EntityEvent {

  /**
   * 更新成功事件
   *
   * @param key 更新的实体key
   */
  case class Updated(key: String) extends EntityEvent

  /**
   * 删除成功事件
   *
   * @param key 删除的实体key值
   */
  case class Deleted(key: String) extends EntityEvent
  //
  //  /**
  //    * 查询结果事件
  //    * @param key 查询的key值
  //    * @param entity 查询的结果
  //    * @param reason 是否查询失败
  //    */
  //  case class Selected(key:String, entity:Option[BeeCacheData], reason:Option[BeeCacheException] = None) extends EntityEvent

  /**
   * 查询结果事件
   *
   * @param key    查询的key值
   * @param result 查询的结果,right：查询到的缓存数据；left：查询异常
   */
  case class Selected(key: String, result: Either[BeeCacheException, BeeCacheData]) extends EntityEvent
}
