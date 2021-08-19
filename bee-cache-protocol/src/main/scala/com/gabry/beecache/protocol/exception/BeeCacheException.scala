package com.gabry.beecache.protocol.exception

import com.gabry.beecache.protocol.EntityMessage

/**
 * Created by gabry on 2018/6/28 17:29
 * 异常类定义
 */
abstract class BeeCacheException(exceptionMessage: String) extends Exception(exceptionMessage) with EntityMessage

/**
 * 未知的异常
 *
 * @param exceptionMessage 异常信息
 */
case class UnknownBeeCacheException(exceptionMessage: String) extends Exception(exceptionMessage)

// 实体操作导致的异常
object EntityException {

  /**
   * 实体key没找到导致的异常
   *
   * @param key              实体key
   * @param exceptionMessage 异常信息
   */
  case class KeyNotFound(key: String, exceptionMessage: String) extends BeeCacheException(exceptionMessage)
}
