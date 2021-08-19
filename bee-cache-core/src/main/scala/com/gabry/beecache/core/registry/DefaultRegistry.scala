package com.gabry.beecache.core.registry

import com.typesafe.config.Config

/**
 * Created by gabry on 2018/7/3 9:13
 */
object DefaultRegistry {
  def apply(config: Config): DefaultRegistry = new DefaultRegistry(config)
}

class DefaultRegistry(config: Config) extends AbstractRegistry(config) {
  /**
   * 获取当前注册中心的类型
   *
   * @return 注册中心的类型
   */
  override def registryType: String = "default"

  /**
   * 开始连接注册中心
   */
  override def connect(): Unit = {}

  /**
   * 断开并重新链接注册中心
   */
  override def reConnect(): Unit = {}

  /**
   * 与注册中心是否已经连接
   *
   * @return true 已经连接
   */
  override def isConnected: Boolean = false

  /**
   * 注册节点
   *
   * @param node 待注册的节点
   * @return 注册结果。true注册成功
   */
  override def registerNode(node: Node): Boolean = false

  /**
   * 注销节点
   *
   * @param node 待注册的节点
   */
  override def unRegisterNode(node: Node): Unit = {}

  /**
   * 按照节点类型返回节点
   *
   * @param nodeType 节点类型
   * @return 节点值列表
   */
  override def getNodesByType(nodeType: String): Array[Node] = Array.empty[Node]

  /**
   * 返回所有节点，包括节点类型、节点值
   *
   * @return 所有节点，包括节点类型、节点值
   */
  override def getAllNodes: Array[Node] = Array.empty[Node]

  /**
   * 端口与注册中心的链接
   */
  override def disConnect(): Unit = {}
}
