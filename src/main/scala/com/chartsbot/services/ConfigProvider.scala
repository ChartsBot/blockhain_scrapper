package com.chartsbot.services

import com.typesafe.config.{ Config, ConfigFactory }

import javax.inject.{ Provider, Singleton }

@Singleton
class ConfigProvider extends Provider[Config] {

  val default: Config = ConfigFactory.load()

  def conf: Config = default

  override def get(): Config = conf

}
