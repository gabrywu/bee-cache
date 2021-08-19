package com.gabry.beecache.client

import com.gabry.beecache.protocol.BeeCacheData
import com.typesafe.config.ConfigFactory

import scala.util.Random

/**
 * Created by gabry on 2018/7/2 13:44
 */
object ClientMain {
  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val client = new BeeCacheClient(config)
    client.initialize()
    val key = new Random().nextInt(1024).toString
    println(s"key = ${key}")
    client.set(BeeCacheData(key, Some("this is value"), 18000)).foreach { res =>
      if (res) {
        val one = client.get("123")
        println(s"one=$one")
        client.delete("123")
        val one1 = client.get("123")
        println(s"one1=$one1")
      }
    }

    client.destroy()
  }
}
