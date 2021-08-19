package com.gabry.beecache.core.registry

/**
 * Created by gabry on 2018/4/17 9:43
 * 节点注册接口
 */
trait Registry {
  /**
   * 获取当前注册中心的类型
   *
   * @return 注册中心的类型
   */
  def registryType: String

  /**
   * 开始连接注册中心
   */
  def connect(): Unit

  /**
   * 断开并重新链接注册中心
   */
  def reConnect(): Unit

  /**
   * 与注册中心是否已经连接
   *
   * @return true 已经连接
   */
  def isConnected: Boolean

  /**
   * 注册节点
   *
   * @param node 待注册的节点
   * @return 注册结果。true注册成功
   */
  def registerNode(node: Node): Boolean

  /**
   * 注销节点
   *
   * @param node 待注册的节点
   */
  def unRegisterNode(node: Node): Unit

  /**
   * 按照节点类型返回节点
   *
   * @param nodeType 节点类型
   * @return 节点值列表
   */
  def getNodesByType(nodeType: String): Array[Node]

  /**
   * 返回所有节点，包括节点类型、节点值
   *
   * @return 所有节点，包括节点类型、节点值
   */
  def getAllNodes: Array[Node]

  /**
   * 端口与注册中心的链接
   */
  def disConnect(): Unit
}
