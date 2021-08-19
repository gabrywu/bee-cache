package com.gabry.beecache.client

import com.typesafe.config.ConfigFactory

/**
 * Created by gabry on 2018/8/21 10:52
 */
object ToManyEntity {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val client = new BeeCacheClient(config)
    client.initialize()
    val entityNumPerShard = 5 * 10000
    val startTime = System.nanoTime
    0 until entityNumPerShard foreach { idx =>
      //client.get(idx.toString)
      client.get(idx.toString)

    }
    val endTime = System.nanoTime
    Thread.sleep(100 * 1000)
    println(s"发送 $entityNumPerShard 条消息 ，耗时 ${(endTime - startTime) / 1000000} 毫秒")
    client.destroy()
  }
}
