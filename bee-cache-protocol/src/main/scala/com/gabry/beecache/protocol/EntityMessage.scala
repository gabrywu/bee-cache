package com.gabry.beecache.protocol

/**
 * Created by gabry on 2018/6/28 18:03
 * 与实体相关的消息。实体代表一个K/V数据。每个实体消息都必须有一个key标志具体的消息
 */
trait EntityMessage extends Message {
  def key: String
}
