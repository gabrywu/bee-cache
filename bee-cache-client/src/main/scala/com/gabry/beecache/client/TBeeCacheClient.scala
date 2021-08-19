package com.gabry.beecache.client

import com.gabry.beecache.protocol.BeeCacheData

import scala.concurrent.Future
import scala.util.Try

/**
 * Created by gabry on 2018/7/2 10:37
 */
trait TBeeCacheDataAsyncOperator {
  def asyncGet(key: String): Future[BeeCacheData]

  def asyncSet(data: BeeCacheData): Future[Boolean]

  def asyncSet(key: String, value: Option[Any], expireTime: Long): Future[Boolean]

  def asyncSetExpire(key: String, expireTime: Long): Future[Boolean]

  def asyncDelete(key: String): Future[Boolean]

  def asyncSelect(key: String): Future[BeeCacheData]
}

trait TBeeCacheDataOperator {
  def get(key: String): Try[BeeCacheData]

  def set(data: BeeCacheData): Try[Boolean]

  def set(key: String, value: Option[Any], expireTime: Long): Try[Boolean]

  def setExpire(key: String, expireTime: Long): Try[Boolean]

  def delete(key: String): Try[Boolean]

  def select(key: String): Try[BeeCacheData]
}

trait TBeeCacheClient {
  /**
   * 初始化客户端
   */
  def initialize(): Unit

  /**
   * 销毁客户端
   */
  def destroy(): Unit
}
