package com.gabry.beecache.client

import com.typesafe.config.Config

/**
 * Created by gabry on 2018/7/2 10:45
 */
abstract class AbstractBeeCacheClient(config: Config) extends TBeeCacheClient with TBeeCacheDataOperator with TBeeCacheDataAsyncOperator {
}
