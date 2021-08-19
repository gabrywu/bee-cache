package com.gabry.beecache.protocol

/**
 * Created by gabry on 2018/6/27 10:30
 * 消息特质。定义最基本的消息形式。消息分为事件和命令。
 * 命令是指让指定actor进行响应的操作。
 * 事件是指命令执行的结果或特定情况下的通知。
 */
trait Message {
  /**
   * 消息发生的时间
   */
  final val at: Long = System.currentTimeMillis()
}
