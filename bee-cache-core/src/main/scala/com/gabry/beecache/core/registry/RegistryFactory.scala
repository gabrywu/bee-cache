package com.gabry.beecache.core.registry

import com.gabry.beecache.core.registry.zookeeper.ZookeeperRegistry
import com.typesafe.config.Config

import scala.util.Try

/**
 * Created by gabry on 2018/4/17 9:54
 * 注册中心工厂类
 */
object RegistryFactory {
  def getRegistryOrDefault(config: Config): AbstractRegistry = getRegistry(config).getOrElse(DefaultRegistry(config))

  /**
   * 返回一个注册中心实例
   *
   * @param config 当前配置
   * @return 注册中心实例
   */
  def getRegistry(config: Config): Try[AbstractRegistry] = Try {
    val registryType = config.getString("registry.type")
    val registryConfig = config.getConfig(s"registry.$registryType")
    registryType.toLowerCase() match {
      case "zookeeper" =>
        new ZookeeperRegistry(registryConfig)
      case otherType =>
        throw new IllegalArgumentException(s"unsupported registry type $otherType")
    }
  }
}
