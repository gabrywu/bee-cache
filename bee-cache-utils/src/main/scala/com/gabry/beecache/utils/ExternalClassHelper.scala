package com.gabry.beecache.utils

import com.typesafe.config._

import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * Created by gabry on 2018/4/16 10:04
 * 用于扩展现有类的功能集合
 */
object ExternalClassHelper {
  /**
   * 提供java.time.Duration到scala.concurrent.duration.Duration的隐式转换
   */
  implicit def javaDuration2FiniteDuration(duration: java.time.Duration): scala.concurrent.duration.Duration =
    scala.concurrent.duration.FiniteDuration(duration.toNanos, scala.concurrent.duration.NANOSECONDS)

  /**
   * 用于扩展com.typesafe.config.Config的功能集合
   */
  implicit class ExternalConfig(val config: Config) extends AnyVal {
    def getIntListOr(path: String, default: java.util.List[Integer]): java.util.List[Integer] =
      if (config.hasPath(path)) config.getIntList(path) else default

    def getLongListOr(path: String, default: java.util.List[java.lang.Long]): java.util.List[java.lang.Long] =
      if (config.hasPath(path)) config.getLongList(path) else default

    def getEnumOr[T <: Enum[T]](enumClass: Class[T], path: String, default: T): T =
      if (config.hasPath(path)) config.getEnum[T](enumClass, path) else default

    def getMemorySizeOr(path: String, default: ConfigMemorySize): ConfigMemorySize =
      if (config.hasPath(path)) config.getMemorySize(path) else default

    def getBytesOr(path: String, default: java.lang.Long): java.lang.Long =
      if (config.hasPath(path)) config.getBytes(path) else default

    def getMillisecondsOr(path: String, default: java.lang.Long): java.lang.Long =
      if (config.hasPath(path)) config.getMilliseconds(path) else default

    def getDurationOr(path: String, unit: TimeUnit, default: Long): Long =
      if (config.hasPath(path)) config.getDuration(path, unit) else default

    def getDurationOr(path: String, default: Duration): Duration =
      if (config.hasPath(path)) config.getDuration(path) else default

    def getListOr(path: String, default: ConfigList): ConfigList =
      if (config.hasPath(path)) config.getList(path) else default

    def getAnyRefOr(path: String, default: AnyRef): AnyRef =
      if (config.hasPath(path)) config.getAnyRef(path) else default

    def getObjectListOr(path: String, default: java.util.List[_ <: ConfigObject]): java.util.List[_ <: ConfigObject] =
      if (config.hasPath(path)) config.getObjectList(path) else default

    def getNumberListOr(path: String, default: java.util.List[Number]): java.util.List[Number] =
      if (config.hasPath(path)) config.getNumberList(path) else default

    def getObjectOr(path: String, default: ConfigObject): ConfigObject =
      if (config.hasPath(path)) config.getObject(path) else default

    def getDoubleListOr(path: String, default: java.util.List[java.lang.Double]): java.util.List[java.lang.Double] =
      if (config.hasPath(path)) config.getDoubleList(path) else default

    def getIntOr(path: String, default: Int): Int =
      if (config.hasPath(path)) config.getInt(path) else default

    def getMemorySizeListOr(path: String, default: java.util.List[ConfigMemorySize]): java.util.List[ConfigMemorySize] =
      if (config.hasPath(path)) config.getMemorySizeList(path) else default

    def getNanosecondsListOr(path: String, default: java.util.List[java.lang.Long]): java.util.List[java.lang.Long] =
      if (config.hasPath(path)) config.getNanosecondsList(path) else default

    def getConfigListOr(path: String, default: java.util.List[_ <: Config]): java.util.List[_ <: Config] =
      if (config.hasPath(path)) config.getConfigList(path) else default

    def getStringListOr(path: String, default: java.util.List[String]): java.util.List[String] =
      if (config.hasPath(path)) config.getStringList(path) else default

    def getBytesListOr(path: String, default: java.util.List[java.lang.Long]): java.util.List[java.lang.Long] =
      if (config.hasPath(path)) config.getBytesList(path) else default

    def getBooleanListOr(path: String, default: java.util.List[java.lang.Boolean]): java.util.List[java.lang.Boolean] =
      if (config.hasPath(path)) config.getBooleanList(path) else default

    def getMillisecondsListOr(path: String, default: java.util.List[java.lang.Long]): java.util.List[java.lang.Long] =
      if (config.hasPath(path)) config.getMillisecondsList(path) else default

    def getDoubleOr(path: String, default: Double): Double =
      if (config.hasPath(path)) config.getDouble(path) else default

    def getNumberOr(path: String, default: Number): Number =
      if (config.hasPath(path)) config.getNumber(path) else default

    def getDurationListOr(path: String, unit: TimeUnit, default: java.util.List[java.lang.Long]): java.util.List[java.lang.Long] =
      if (config.hasPath(path)) config.getDurationList(path, unit) else default

    def getDurationListOr(path: String, default: java.util.List[Duration]): java.util.List[Duration] =
      if (config.hasPath(path)) config.getDurationList(path) else default

    def getBooleanOr(path: String, default: Boolean): Boolean =
      if (config.hasPath(path)) config.getBoolean(path) else default

    def getEnumListOr[T <: Enum[T]](enumClass: Class[T], path: String, default: java.util.List[T]): java.util.List[T] =
      if (config.hasPath(path)) config.getEnumList(enumClass, path) else default

    def getStringOr(path: String, default: String): String =
      if (config.hasPath(path)) config.getString(path) else default

    def getConfigOr(path: String, default: Config): Config =
      if (config.hasPath(path)) config.getConfig(path) else default

    def getLongOr(path: String, default: Long): Long =
      if (config.hasPath(path)) config.getLong(path) else default

    def getValueOr(path: String, default: ConfigValue): ConfigValue =
      if (config.hasPath(path)) config.getValue(path) else default

    def getNanosecondsOr(path: String, default: java.lang.Long): java.lang.Long =
      if (config.hasPath(path)) config.getNanoseconds(path) else default

    def getAnyRefListOr(path: String, default: java.util.List[_]): java.util.List[_] =
      if (config.hasPath(path)) config.getAnyRefList(path) else default
  }

}
