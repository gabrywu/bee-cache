package com.gabry.beecache.protocol

/**
 * Created by gabry on 2018/6/27 10:08
 * 实体数据。包括key、value、expireTime（过期时间）、version版本号
 */
case class BeeCacheData(key: String, value: Option[Any], expireTime: Long, version: Long = -1) {
  final val createTime: Long = System.currentTimeMillis()

  override def toString: String = s"key[$key],value[$value],expire[$expireTime],createTime[$createTime],version[$version]"
}
