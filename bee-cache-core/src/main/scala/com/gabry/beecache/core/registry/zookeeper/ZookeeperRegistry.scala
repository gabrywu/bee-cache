package com.gabry.beecache.core.registry.zookeeper

import com.gabry.beecache.core.registry.{AbstractRegistry, Node}
import com.typesafe.config.Config
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.framework.{CuratorFramework, CuratorFrameworkFactory}
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.curator.utils.ZKPaths
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.data.Stat

import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters._
import scala.util.Try


object ZookeeperRegistry {
  private val slashPlaceholder = "_" // zookeeper节点路径中斜杠字符的替换字符
  private val KEY_HOST = "hosts"
  private val KEY_BASE_SLEEP_TIME_MS = "exponential-backoff-retry.base-sleep-timeMs"
  private val KEY_MAX_RETRIES = "exponential-backoff-retry.max-retries"
  private val KEY_ROOT_PATH = "root-path"
}

/**
 * Created by gabry on 2018/4/17 9:56
 * Zookeeper注册中心类
 */
class ZookeeperRegistry(config: Config) extends AbstractRegistry(config) {

  import com.gabry.beecache.utils.ExternalClassHelper._

  private val hosts: String = config.getStringOr(ZookeeperRegistry.KEY_HOST, "localhost:2181")
  private val baseSleepTimeMs: Int = config.getIntOr(ZookeeperRegistry.KEY_BASE_SLEEP_TIME_MS, 1000)
  private val maxRetries: Int = config.getIntOr(ZookeeperRegistry.KEY_MAX_RETRIES, 3)
  private val rootPath: String = config.getStringOr(ZookeeperRegistry.KEY_ROOT_PATH, "/zkClientHelper")
  private var zkClient: CuratorFramework = _

  /**
   * 断开并重新链接注册中心
   */
  def reConnect(): Unit = {
    disConnect()
    connect()
  }

  /**
   * 开始连接注册中心
   */
  override def connect(): Try[Unit] = Try {
    if (!isConnected) {
      zkClient = CuratorFrameworkFactory.newClient(hosts, new ExponentialBackoffRetry(baseSleepTimeMs, maxRetries))
      zkClient.start()
      if (zkClient.blockUntilConnected(baseSleepTimeMs, TimeUnit.SECONDS)) {
        checkRootNode()
      }
    }
  }

  private def checkRootNode(): Try[Unit] = Try {
    val nodeCheck = zkClient.checkExists().forPath(s"$rootPath")
    if (nodeCheck == null) {
      zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(s"$rootPath", "".getBytes)

    }
  }

  /**
   * 与注册中心是否已经连接
   *
   * @return true 已经连接
   */
  override def isConnected: Boolean = zkClient != null && zkClient.getState == CuratorFrameworkState.STARTED

  /**
   * 端口与注册中心的链接
   */
  override def disConnect(): Unit = {
    zkClient.close()
  }

  /**
   * 注册节点
   *
   * @param node 待注册的节点
   * @return 注册结果。true注册成功
   */
  override def registerNode(node: Node): Boolean = register(node.nodeType, node.anchor)

  private def register(registerType: String, anchor: String) = {
    val path = pathFor(registerType, anchor)
    var isSuccess = false
    try {
      val pathCheck: Stat = zkClient.checkExists().forPath(path)
      if (pathCheck != null) {
        zkClient.delete().forPath(path)
      }
      zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path, anchor.getBytes)
      isSuccess = true
    } catch {
      case ex: Exception =>
        ex.printStackTrace()
    }
    isSuccess
  }

  private def pathFor(registerType: String, anchor: String): String = {
    val anchorChild = anchor.replaceAll(ZKPaths.PATH_SEPARATOR, ZookeeperRegistry.slashPlaceholder)
    s"$rootPath/$registerType-$anchorChild"
  }

  /**
   * 注销节点
   *
   * @param node 待注册的节点
   */
  override def unRegisterNode(node: Node): Unit = unRegister(node.nodeType, node.anchor)

  private def unRegister(registerType: String, anchor: String): Unit = {
    val path = pathFor(registerType, anchor)
    try {
      val pathCheck = zkClient.checkExists().forPath(path)
      if (pathCheck != null) {
        zkClient.delete().forPath(path)
      }
    } catch {
      case ex: Exception =>
    }
  }

  /**
   * 按照节点类型返回节点
   *
   * @param nodeType 节点类型
   * @return 节点值列表
   */
  override def getNodesByType(nodeType: String): Array[Node] = getLeafNodeByType(nodeType)

  private def getLeafNodeByType(nodeType: String): Array[Node] = {
    try {
      val leafNodes = zkClient.getChildren.forPath(s"$rootPath").asScala.map { child =>
        val children = child.split("-")
        (children(0), children(1))
      }.filter(_._1 == nodeType).map(leafNode => Node(leafNode._1, leafNode._2.replaceAll(ZookeeperRegistry.slashPlaceholder, ZKPaths.PATH_SEPARATOR))).toArray
      leafNodes
    } catch {
      case ex: Exception =>
        Array.empty
    }
  }

  /**
   * 返回所有节点，包括节点类型、节点值
   *
   * @return 所有节点，包括节点类型、节点值
   */
  override def getAllNodes: Array[Node] = {
    try {
      zkClient.getChildren.forPath(rootPath).asScala.map { typeAndAnchor =>
        val nodeType = typeAndAnchor.split("-")(0)
        val nodeAnchor = typeAndAnchor.split("-")(1)
        Node(nodeType, nodeAnchor.replaceAll(ZookeeperRegistry.slashPlaceholder, ZKPaths.PATH_SEPARATOR))
      }.toArray
    } catch {
      case ex: Exception =>
        Array.empty
    }
  }

  /**
   * 获取当前注册中心的类型
   *
   * @return 注册中心的类型
   */
  override def registryType: String = "zookeeper"
}
