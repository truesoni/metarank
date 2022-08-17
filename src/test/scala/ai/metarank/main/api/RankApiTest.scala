package ai.metarank.main.api

import ai.metarank.fstore.memory.MemPersistence
import ai.metarank.source.ModelCache
import ai.metarank.util.{RandomScorer, TestFeatureMapping, TestModelCache, TestRankingEvent}
import cats.effect.unsafe.implicits.global
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Random

class RankApiTest extends AnyFlatSpec with Matchers {
  lazy val random  = new Random(1)
  lazy val mapping = TestFeatureMapping()
  lazy val store   = MemPersistence(mapping.schema)
  lazy val models  = TestModelCache(RandomScorer())
  lazy val service = RankApi(mapping, store, models)

  it should "respond with the same data reranked" in {
    val response =
      service.rerank(mapping, TestRankingEvent(List("p1", "p2", "p3")), "random", explain = false).unsafeRunSync()
    response.items.map(_.item.value) shouldBe List("p1", "p3", "p2")
  }

  it should "emit feature values" in {
    val response =
      service.rerank(mapping, TestRankingEvent(List("p1", "p2", "p3")), "random", explain = true).unsafeRunSync()
    response.items.forall(_.features.size == 5) shouldBe true
  }

}