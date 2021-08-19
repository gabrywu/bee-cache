# BeeCache

## 介绍

BeeCache是一个简单的分布式缓存系统，它基于Akka Persistence/Cluster Sharding构建。提供K/V数据的最基本的增删改查，设置过期时间等功能。

## 项目结构

* bee-cache-client。缓存系统客户端，通过它与服务端通信，对K/V数据进行存取
* bee-cache-core。服务端核心代码
* bee-cache-protocol。系统用到的核心消息定义
* bee-cache-utils。工具包
