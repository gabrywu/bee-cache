package com.gabry.beecache.core.registry

/**
 * Created by gabry on 2018/6/29 15:19
 */
final case class Node(nodeType: String, anchor: String) {
  override def toString: String = s"Node($nodeType,$anchor)"
}
