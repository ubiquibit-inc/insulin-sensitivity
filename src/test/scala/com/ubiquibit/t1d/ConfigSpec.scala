package com.ubiquibit.t1d

import com.typesafe.config.{Config, ConfigException, ConfigFactory}
import org.scalatest.FunSpec

import scala.language.implicitConversions

/**
  * This work (c) by jason@ubiquibit.com
  *
  * This work is licensed under a
  * Creative Commons Attribution-ShareAlike 4.0 International License.
  *
  * You should have received a copy of the license along with this
  *   work.  If not, see <http://creativecommons.org/licenses/by-sa/4.0/>.
  */
class ConfigSpec extends FunSpec {

  describe("Personal Data configuration") {

    it("should not load testIds from default config") {
      val config: Config = ConfigFactory.load()

      intercept[ConfigException.Missing] {
        config.getIntList("testIds")
      }
    }

    it("should load testIds from personaldata.properties") {
      val config: Config = ConfigFactory.parseResources("personaldata.properties")
      val idsAsStr = config.getString("testIds")
      val ids = idsAsStr
        .replace("]", "")
        .replace("[", "")
        .split(",").map{ str => str.toInt}
      assert(ids.size === Integer.valueOf(2))

      val first = ids.head
      assert(first > 20000000)
      assert(first < 30000000)
    }
  }
}
