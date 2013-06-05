package epic.features

import epic.util.{CacheBroker, Has2}
import breeze.util.Index
import epic.framework.Feature
import scala.collection.mutable

trait IndexedWordFeaturizer[W] {
  def featureIndex: Index[Feature]
  def featurizer: WordFeaturizer[W]
  def anchor(datum: IndexedSeq[W]):IndexedWordAnchoring[W]
}

/**
 *
 * @author dlwh
 */
object IndexedWordFeaturizer {
  def fromData[W](feat: WordFeaturizer[W],
                  data: IndexedSeq[IndexedSeq[W]],
                  wordHashFeatures: Int = 0): IndexedWordFeaturizer[W]  = {
    val wordIndex = Index[Feature]()
    for(words <- data) {
      val anch = feat.anchor(words)
      for(i <- 0 until words.length) {
        anch.featuresForWord(i) foreach {wordIndex.index _}
      }
    }

    val cache : CacheBroker = CacheBroker()


    new MyWordFeaturizer[W](feat, wordIndex, cache.make("epic.features.indexed_word_features"+feat.hashCode()))
  }

  @SerialVersionUID(1L)
  private class MyWordFeaturizer[W](val featurizer: WordFeaturizer[W],
                                    val featureIndex: Index[Feature],
                                    cache: mutable.Map[IndexedSeq[W], IndexedWordAnchoring[W]]) extends IndexedWordFeaturizer[W] with Serializable {
    def anchor(words: IndexedSeq[W]):IndexedWordAnchoring[W]  = cache.getOrElseUpdate(words, {
      val anch = featurizer.anchor(words)
      val wordFeatures = Array.tabulate(words.length, FeaturizationLevel.numLevels) { (i,l) => stripEncode(featureIndex, anch.featuresForWord(i, l))}

      new TabulatedIndexedSurfaceAnchoring[W](words, wordFeatures, null)
    })
  }

  def stripEncode(ind: Index[Feature], features: Array[Feature]) = {
    val result = mutable.ArrayBuilder.make[Int]()
    result.sizeHint(features)
    var i = 0
    while(i < features.length) {
      val fi = ind(features(i))
      if(fi >= 0)
        result += fi
      i += 1
    }
    val r = result.result()
    assert(!r.isEmpty, features.toIndexedSeq + " " + ind)
    r
  }
}

