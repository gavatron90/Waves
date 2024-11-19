package com.wavesplatform.settings

import com.typesafe.config.ConfigFactory
import com.wavesplatform.test.FlatSpec
import com.wavesplatform.transaction.TxHelpers
import pureconfig.ConfigSource
import pureconfig.generic.auto.*

import scala.concurrent.duration.*

class MinerSettingsSpecification extends FlatSpec {
  "MinerSettings" should "read values" in {
    val config = ConfigFactory
      .parseString(s"""
                      |waves {
                      |  miner {
                      |    enable: yes
                      |    quorum: 1
                      |    interval-after-last-block-then-generation-is-allowed: 1d
                      |    no-quorum-mining-delay = 5s
                      |    micro-block-interval: 5s
                      |    minimal-block-generation-offset: 500ms
                      |    max-transactions-in-micro-block: 400
                      |    min-micro-block-age: 3s
                      |    private-keys: ["${TxHelpers.defaultSigner.privateKey}"]
                      |  }
                      |}
      """.stripMargin)
      .resolve()

    val settings = ConfigSource.fromConfig(config).at("waves.miner").loadOrThrow[MinerSettings]

    settings.enable should be(true)
    settings.quorum should be(1)
    settings.microBlockInterval should be(5.seconds)
    settings.noQuorumMiningDelay should be(5.seconds)
    settings.minimalBlockGenerationOffset should be(500.millis)
    settings.maxTransactionsInMicroBlock should be(400)
    settings.minMicroBlockAge should be(3.seconds)
    settings.privateKeys should be(Seq(TxHelpers.defaultSigner.privateKey))
  }
}
