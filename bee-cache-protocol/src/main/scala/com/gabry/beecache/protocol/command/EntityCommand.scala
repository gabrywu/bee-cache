package com.gabry.beecache.protocol.command

import com.gabry.beecache.protocol.EntityMessage

/**
 * Created by gabry on 2018/6/28 18:04
 * 与实体相关的命令。比如Get/Set/Delete/SetExpire/Select
 */
sealed trait EntityCommand extends Command with EntityMessage

object EntityCommand {
  sealed trait Update extends EntityCommand

  /**
   * 设置key对应的值
   *
   * @param key        待设置的key
   * @param value      key对应的值
   * @param expireTime key对应的超时时间
   */
  case class Set(key: String, value: Any, expireTime: Long) extends Update

  /**
   * 设置key对应的超时时间
   *
   * @param key        待设置的key
   * @param expireTime 超时时间
   */
  case class SetExpire(key: String, expireTime: Long) extends Update

  /**
   * 删除key对应的值
   *
   * @param key 待删除的key
   */
  case class Delete(key: String) extends EntityCommand

  /**
   * 查询key对应的值
   *
   * @param key 待查询的key
   */
  case class Get(key: String) extends EntityCommand

  /**
   * 查询key是否存在
   * 如果存在，则语义等同Get；不存在则抛KeyNotFound的异常
   *
   * @param key 待查询的key
   */
  case class Select(key: String) extends EntityCommand
}
