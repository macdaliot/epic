package epic.sequences

import breeze.util.{Index, OptionIndex}
import epic.trees.Span
import breeze.numerics
import epic.sequences.SemiCRF.Marginal
import breeze.features.FeatureVector
import epic.framework.{Feature, ModelObjective, VisitableMarginal}
import java.util

import scala.collection.mutable.ArrayBuffer
import util.concurrent.ConcurrentHashMap

import collection.immutable.BitSet
import breeze.collection.mutable.TriangularArray
import java.io.{IOException, ObjectInputStream}

import breeze.optimize.FirstOrderMinimizer.OptParams
import breeze.optimize.CachedBatchDiffFunction
import breeze.linalg.{DenseVector, softmax}
import epic.constraints.{LabeledSpanConstraints, TagConstraints}
import epic.constraints.LabeledSpanConstraints.NoConstraints
import epic.util.Optional
import epic.features.{SurfaceFeaturizer, WordFeaturizer}
import scala.util.control.Breaks._

import scala.util.Random

/**
  * A Semi-Markov Linear Chain Conditional Random Field, that is, the length
  * of time spent in a state may be longer than 1 tick. Useful for field segmentation or NER.
  *
  * As usual in Epic, all the heavy lifting is done in the companion object and Marginals.
  *
  * @author dlwh
  */
@SerialVersionUID(1L)
trait SemiCRF[L, W] extends Serializable {
  def scorer(w: IndexedSeq[W]): SemiCRF.Anchoring[L, W]

  def labelIndex: OptionIndex[L]

  def marginal(w: IndexedSeq[W]) = {
    SemiCRF.Marginal(scorer(w))
  }

  def goldMarginal(segmentation: IndexedSeq[(L, Span)], w: IndexedSeq[W]): Marginal[L, W] = {
    SemiCRF.Marginal.goldMarginal(scorer(w), segmentation)
  }

  def getPosteriors(w: IndexedSeq[W], labels: ArrayBuffer[Array[Int]]): Array[Double] = {
    val bestLab = getBestLabel(w)
    var alreadyContains = false
    breakable {
      for (i <- labels.indices) {
        if ((labels(i): Seq[Int]) == (bestLab: Seq[Int])) {
          alreadyContains = true
          break
        }
      }
    }
    if (!alreadyContains) {
      labels += bestLab
    }
    SemiCRF.bestLabelScore(marginal(w), labels.toArray)
  }

  def leastConfidence(w: IndexedSeq[W]): Double = {
    SemiCRF.getBestScore(marginal(w))
  }

  def getScoreOfLabel(w: IndexedSeq[W], label: Array[Int]): Double = {
    val labels = Array.ofDim[Int](1, label.length)
    labels(0) = label
    val score = SemiCRF.bestLabelScore(marginal(w), labels)
    score(0)
  }

  def getLabels(w: IndexedSeq[W]): ArrayBuffer[Array[Int]] = {
    val bestScore = leastConfidence(w)
    //println("BestScore: "+ bestScore)
    SemiCRF.makeLabels(marginal(w), bestScore)
  }

  def bestSequence(w: IndexedSeq[W], id: String = ""): Segmentation[L, W] = {
    SemiCRF.posteriorDecode(marginal(w), id)
  }

  def getBestLabel(w: IndexedSeq[W]): Array[Int] = {
    val taggedSequence = bestSequence(w).asFlatTaggedSequence
    var bestLab = taggedSequence.toString.substring(taggedSequence.toString.indexOf("label = ArrayBuffer(") +
      "label = ArrayBuffer(".length())
    bestLab = bestLab.substring(0, bestLab.indexOf("), features"))
    var bestLabVec = bestLab.split(", ")
    var label = Array.fill(bestLabVec.length)(100)
    for (i <- 0 until bestLabVec.length) {
      if (bestLabVec(i) == "None") {
        label(i) = 2
      }
      else if (bestLabVec(i) == "Some(B_MALWARE)") {
        label(i) = 0
      }

      else if (bestLabVec(i) == "Some(I_MALWARE)") {
        label(i) = 1
      }

    }
    return label

  }

}

object SemiCRF {


  def buildSimple[L](data: IndexedSeq[Segmentation[L, String]],
                     gazetteer: Gazetteer[Any, String] = Gazetteer.empty[Any, String],
                     wordFeaturizer: Optional[WordFeaturizer[String]] = None,
                     spanFeaturizer: Optional[SurfaceFeaturizer[String]] = None,
                     opt: OptParams = OptParams(regularization = 1.0)): SemiCRF[L, String] = {
    val model: SemiCRFModel[L, String] = new SegmentationModelFactory[L](gazetteer = gazetteer, wordFeaturizer = wordFeaturizer, spanFeaturizer = spanFeaturizer).makeModel(data)

    val obj = new ModelObjective(model, data)
    val cached = new CachedBatchDiffFunction(obj)
    val weights = opt.minimize(cached, obj.initialWeightVector(false))
    //    GradientTester.test(cached, weights, randFraction = 1.0, toString={(i: Int) => model.featureIndex.get(i).toString}, tolerance=0.0)
    val crf = model.extractCRF(weights)

    crf
  }

  def buildIOModel[L](data: IndexedSeq[Segmentation[L, String]],
                      gazetteer: Gazetteer[Any, String] = Gazetteer.empty[Any, String],
                      opt: OptParams = OptParams()): SemiCRF[Unit, String] = {
    val fixedData: IndexedSeq[Segmentation[Unit, String]] = data.map { s =>
      s.copy(segments = s.segments.map { case (l, span) => ((), span) })
    }
    buildSimple(fixedData, gazetteer, opt = opt)
  }

  def fromCRF[L, W](crf: CRF[L, W]): SemiCRF[L, W] = new SemiCRF[L, W] {

    def scorer(w: IndexedSeq[W]): Anchoring[L, W] = new Anchoring[L, W] {
      val anch = crf.anchor(w)

      val constraints: LabeledSpanConstraints[L] = {
        LabeledSpanConstraints.fromTagConstraints(anch)
      }

      def words: IndexedSeq[W] = w

      def scoreTransition(prev: Int, cur: Int, begin: Int, end: Int): Double = {
        if (prev == labelIndex(None) && begin == 0) {
          scoreTransition(labelIndex.apply(Some(crf.startSymbol)), cur, begin, end)
        } else if (end - begin != 1 || prev == crf.labelIndex.size || cur == crf.labelIndex.size) {
          Double.NegativeInfinity
        } else {
          anch.scoreTransition(begin, prev, cur)
        }
      }

      val labelIndex = new OptionIndex(crf.labelIndex)

    }

    override def labelIndex: OptionIndex[L] = new OptionIndex(crf.labelIndex)
  }


  /**
    * An Anchoring encodes all the information needed to score Semimarkov models.
    *
    * In particular, it can score transitions between a previous label (prev)
    * and the next label, which spans from begin to end.
    *
    * @tparam L
    * @tparam W
    */
  trait Anchoring[L, W] {
    def words: IndexedSeq[W]

    def length: Int = words.length

    def constraints: LabeledSpanConstraints[L]

    def maxSegmentLength(label: Int): Int = if (label >= labelIndex.size - 1) 1 else constraints.maxSpanLengthForLabel(label)

    def scoreTransition(prev: Int, cur: Int, begin: Int, end: Int): Double

    def labelIndex: OptionIndex[L]

    def ignoreTransitionModel: Boolean = false

    def *(other: Anchoring[L, W]): Anchoring[L, W] = {
      (this, other) match {
        case (x: IdentityAnchoring[L, W], _) => other
        case (_, x: IdentityAnchoring[L, W]) => this
        case (x, y) => new ProductAnchoring(this, other)
      }
    }
  }

  /**
    * A visitor used by [[epic.sequences.SemiCRF.Marginal]] for giving
    * marginal probabilities over labeled spans.
    *
    * @tparam L
    * @tparam W
    */
  trait TransitionVisitor[L, W] {
    def visitTransition(prev: Int, cur: Int, begin: Int, end: Int, count: Double)
  }

  trait Marginal[L, W] extends VisitableMarginal[TransitionVisitor[L, W]] {

    def anchoring: Anchoring[L, W]

    def words: IndexedSeq[W] = anchoring.words

    def length: Int = anchoring.length

    /** Visits spans with non-zero score, useful for expected counts */
    def visit(f: TransitionVisitor[L, W])

    /** normalized probability of seeing segment with transition */
    def transitionMarginal(prev: Int, cur: Int, begin: Int, end: Int): Double

    def logPartition: Double

    def spanMarginal(cur: Int, begin: Int, end: Int): Double = {
      var prev = 0
      val numLabels: Int = anchoring.labelIndex.size
      var sum = 0.0
      while (prev < numLabels) {
        sum += transitionMarginal(prev, cur, begin, end)
        prev += 1
      }
      sum
    }

    def spanMarginal(begin: Int, end: Int): DenseVector[Double] = DenseVector.tabulate(anchoring.labelIndex.size)(spanMarginal(_, begin, end))

    def computeSpanConstraints(threshold: Double = 1E-5): LabeledSpanConstraints[L] = {
      val spanMarginals = TriangularArray.fill(length + 1)(new Array[Double](anchoring.labelIndex.size))

      this visit new TransitionVisitor[L, W] {
        def visitTransition(prev: Int, cur: Int, begin: Int, end: Int, count: Double) {
          spanMarginals(begin, end)(cur) += count
        }
      }

      val allowedLabels = spanMarginals.map { arr =>
        BitSet.empty ++ (0 until arr.length).filter(i => arr(i) >= threshold)
        //           BitSet.empty ++ (0 until arr.length)
      }

      LabeledSpanConstraints(allowedLabels)
    }

    def hasSupportOver(m: Marginal[L, W]): Boolean = {
      object FailureException extends Exception
      try {
        m visit new TransitionVisitor[L, W] {
          def visitTransition(prev: Int, cur: Int, begin: Int, end: Int, count: Double) {
            if (count >= 0.0 && transitionMarginal(prev, cur, begin, end) <= 0.0) {
              throw FailureException
            }
          }
        }
        true
      } catch {
        case FailureException => false
      }
    }

    def decode: String = {
      val buf = new StringBuilder()
      this visit new TransitionVisitor[L, W] {
        def visitTransition(prev: Int, cur: Int, begin: Int, end: Int, count: Double) {
          if (count != 0) {
            buf ++= s"${anchoring.labelIndex.get(prev)} ${anchoring.labelIndex.get(cur)} ($begin,$end) $count\n"
          }
        }
      }
      buf.result()
    }
  }

  object Marginal {

    def maxDerivationMarginal[L, W](scorer: Anchoring[L, W]): Marginal[L, W] = {
      val maxDerivation: Segmentation[L, W] = viterbi(scorer)
      goldMarginal(scorer, maxDerivation.label)
    }

    def apply[L, W](scorer: Anchoring[L, W]): Marginal[L, W] = {

      val forwardScores: Array[Array[Double]] = this.forwardScores(scorer)
      val backwardScore: Array[Array[Double]] = this.backwardScores(scorer, forwardScores)
      //println("Backwardscores: " + backwardScore.deep.mkString("\n"))
      //println("Forwardscores: " +forwardScores.deep.mkString("\n"))
      //println("Softmax of: " + forwardScores.last.mkString(" "))
      val partition = softmax(forwardScores.last)
      val _s = scorer


      new Marginal[L, W] {

        def anchoring: Anchoring[L, W] = _s

        /** Visits spans with non-zero score, useful for expected counts */
        def visit(f: TransitionVisitor[L, W]) {
          val numLabels = scorer.labelIndex.size

          var begin = length - 1
          while (begin >= 0) {
            var prevLabel = 0
            while (prevLabel < numLabels) {
              val forwardPrev = forwardScores(begin)(prevLabel)
              if (forwardPrev != Double.NegativeInfinity) {
                var end = math.min(length, anchoring.constraints.maxSpanLengthStartingAt(begin) + begin)
                while (end > begin) {
                  if (anchoring.constraints.isAllowedSpan(begin, end)) {
                    var label = 0
                    while (label < numLabels) {
                      if (anchoring.constraints.isAllowedLabeledSpan(begin, end, label)) {
                        val prevScore = backwardScore(end)(label)
                        if (anchoring.maxSegmentLength(label) >= end - begin && prevScore != Double.NegativeInfinity) {
                          val score = transitionMarginal(prevLabel, label, begin, end)
                          if (score != 0.0) {
                            f.visitTransition(prevLabel, label, begin, end, score)
                          }
                        }
                      }

                      label += 1
                    }
                  }
                  end -= 1
                }
              }

              prevLabel += 1
            }

            begin -= 1

          }

        }


        /** Log-normalized probability of seing segment with transition */
        def transitionMarginal(prev: Int, cur: Int, begin: Int, end: Int): Double = {
          val withoutTrans = forwardScores(begin)(prev) + backwardScore(end)(cur)
          if (withoutTrans.isInfinite) 0.0
          else {
            math.exp(withoutTrans + anchoring.scoreTransition(prev, cur, begin, end) - logPartition)
          }
        }

        def logPartition: Double = partition
      }

    }

    def goldMarginal[L, W](scorer: Anchoring[L, W], segments: IndexedSeq[(L, Span)]): Marginal[L, W] = {
      var lastSymbol = scorer.labelIndex(None)
      var score = 0.0
      var lastEnd = 0
      val goldEnds = Array.fill(scorer.length)(-1)
      val goldLabels = Array.fill(scorer.length)(-1)
      val goldPrevLabels = Array.fill(scorer.length)(-1)
      val segmentation: Segmentation[L, W] = new Segmentation(segments, scorer.words)
      for ((l, span) <- segmentation.segmentsWithOutside) {
        assert(span.begin == lastEnd)
        val symbol = scorer.labelIndex(l)
        assert(symbol != -1, s"$l not in index: ${scorer.labelIndex}")
        assert(scorer.constraints.isAllowedLabeledSpan(span.begin, span.end, symbol))
        score += scorer.scoreTransition(lastSymbol, symbol, span.begin, span.end)
        assert(!score.isInfinite, " " + segments + " " + l + " " + span)
        goldEnds(span.begin) = span.end
        goldLabels(span.begin) = symbol
        goldPrevLabels(span.begin) = lastSymbol
        lastSymbol = symbol
        lastEnd = span.end
      }

      val s = scorer

      new Marginal[L, W] {

        def anchoring: Anchoring[L, W] = s

        /** Visits spans with non-zero score, useful for expected counts */
        def visit(f: TransitionVisitor[L, W]) {
          var lastSymbol = scorer.labelIndex(None)
          var lastEnd = 0
          for ((l, span) <- segmentation.segmentsWithOutside) {
            assert(span.begin == lastEnd)
            val symbol = scorer.labelIndex(l)
            f.visitTransition(lastSymbol, symbol, span.begin, span.end, 1.0)
            lastEnd = span.end
            lastSymbol = symbol
          }

        }

        /** normalized probability of seeing segment with transition */
        def transitionMarginal(prev: Int, cur: Int, begin: Int, end: Int): Double = {
          numerics.I(goldEnds(begin) == end && goldLabels(begin) == cur && goldPrevLabels(begin) == prev)
        }


        def logPartition: Double = score
      }
    }

    /**
      *
      * @param anchoring
      * @return forwardScore(end position)(label) = forward score of ending a segment labeled label in position end position
      */
    private def forwardScores[L, W](anchoring: SemiCRF.Anchoring[L, W]): Array[Array[Double]] = {
      val length = anchoring.length
      val numLabels = anchoring.labelIndex.size
      // total weight (logSum) for ending in pos with label l.
      val forwardScores = Array.fill(length + 1, numLabels)(Double.NegativeInfinity)
      forwardScores(0)(anchoring.labelIndex(None)) = 0.0

      val accumArray = new Array[Double](numLabels * length)

      var end = 1
      while (end <= length) {
        var label = 0
        while (label < numLabels) {
          var acc = 0
          var begin = math.max(end - anchoring.maxSegmentLength(label), 0)
          while (begin < end) {
            if (anchoring.constraints.isAllowedLabeledSpan(begin, end, label)) {
              var prevLabel = 0
              if (anchoring.ignoreTransitionModel) {
                prevLabel = -1 // ensure that you don't actually need the transition model
                val prevScore = softmax.array(forwardScores(begin), forwardScores(begin).length)
                if (prevScore != Double.NegativeInfinity) {
                  val score = anchoring.scoreTransition(prevLabel, label, begin, end) + prevScore
                  if (score != Double.NegativeInfinity) {
                    accumArray(acc) = score
                    acc += 1
                  }
                }
              } else {
                while (prevLabel < numLabels) {
                  val prevScore = forwardScores(begin)(prevLabel)
                  if (prevScore != Double.NegativeInfinity) {
                    val score = anchoring.scoreTransition(prevLabel, label, begin, end) + prevScore
                    if (score != Double.NegativeInfinity) {
                      accumArray(acc) = score
                      acc += 1
                    }
                  }

                  prevLabel += 1
                }
              }
            }

            begin += 1
          }
          forwardScores(end)(label) = softmax.array(accumArray, acc)
          label += 1
        }

        end += 1
      }
      forwardScores
    }

    /**
      * computes the sum of all derivations, starting from a label that ends at pos, and ending
      * at the end of the sequence
      *
      * @param anchoring anchoring to score spans
      * @tparam L label type
      * @tparam W word type
      * @return backwardScore(pos)(label)
      */
    private def backwardScores[L, W](anchoring: SemiCRF.Anchoring[L, W], forwardScores: Array[Array[Double]]): Array[Array[Double]] = {
      val length = anchoring.length
      val numLabels = anchoring.labelIndex.size
      // total completion weight (logSum) for starting from an end at pos with label l
      val backwardScores = Array.fill(length + 1, numLabels)(Double.NegativeInfinity)
      util.Arrays.fill(backwardScores(length), 0.0)

      val maxOfSegmentLengths = (0 until numLabels).map(anchoring.maxSegmentLength _).max

      val accumArray = new Array[Double](numLabels * math.min(maxOfSegmentLengths, length))

      var begin = length - 1
      while (begin >= 0) {
        var prevLabel = 0
        while (prevLabel < numLabels) {
          if (forwardScores(begin)(prevLabel) != Double.NegativeInfinity) {
            var acc = 0 // index into accumArray
            var end = math.min(anchoring.constraints.maxSpanLengthStartingAt(begin) + begin, length)
            while (end > begin) {
              if (anchoring.constraints.isAllowedSpan(begin, end)) {
                var label = 0
                while (label < numLabels) {
                  if (anchoring.constraints.isAllowedLabeledSpan(begin, end, label)) {
                    val prevScore = backwardScores(end)(label)
                    if (anchoring.maxSegmentLength(label) >= end - begin && prevScore != Double.NegativeInfinity) {
                      val score = anchoring.scoreTransition(prevLabel, label, begin, end) + prevScore
                      if (score != Double.NegativeInfinity) {
                        accumArray(acc) = score
                        acc += 1
                      }
                    }
                  }

                  label += 1
                }
              }
              end -= 1
            }

            if (acc > 0)
              backwardScores(begin)(prevLabel) = softmax(new DenseVector(accumArray, 0, 1, acc))
          }
          prevLabel += 1
        }

        begin -= 1

      }

      backwardScores
    }


  }

  trait ConstraintSemiCRF[L, W] extends SemiCRF[L, W] with LabeledSpanConstraints.Factory[L, W] {
    def constraints(w: IndexedSeq[W]): LabeledSpanConstraints[L]

    def constraints(seg: Segmentation[L, W], keepGold: Boolean = true): LabeledSpanConstraints[L]
  }

  @SerialVersionUID(1L)
  class IdentityConstraintSemiCRF[L, W](val labelIndex: OptionIndex[L]) extends ConstraintSemiCRF[L, W] with Serializable {
    outer =>
    def scorer(w: IndexedSeq[W]) = new Anchoring[L, W]() {
      def words = w

      def scoreTransition(prev: Int, cur: Int, begin: Int, end: Int) = 0.0

      def labelIndex = outer.labelIndex

      def constraints: LabeledSpanConstraints[L] = NoConstraints
    }


    def constraints(w: IndexedSeq[W]) = NoConstraints

    def constraints(seg: Segmentation[L, W], keepGold: Boolean) = NoConstraints
  }

  @SerialVersionUID(1L)
  class BaseModelConstraintSemiCRF[L, W](val crf: SemiCRF[L, W], val threshold: Double = 1E-5) extends ConstraintSemiCRF[L, W] with Serializable {
    def labelIndex = crf.labelIndex

    // TODO: make weak
    @transient
    private var cache = new ConcurrentHashMap[IndexedSeq[W], LabeledSpanConstraints[L]]()

    // Don't delete.
    @throws(classOf[IOException])
    @throws(classOf[ClassNotFoundException])
    private def readObject(oin: ObjectInputStream) {
      oin.defaultReadObject()
      cache = new ConcurrentHashMap[IndexedSeq[W], LabeledSpanConstraints[L]]()
    }

    def constraints(w: IndexedSeq[W]): LabeledSpanConstraints[L] = {
      var c = cache.get(w)
      if (c eq null) {
        c = crf.marginal(w).computeSpanConstraints(threshold)
        cache.put(w, c)
      }

      c
    }

    def constraints(seg: Segmentation[L, W], keepGold: Boolean = true): LabeledSpanConstraints[L] = {
      val orig: LabeledSpanConstraints[L] = constraints(seg.words)
      if (keepGold) {
        orig | crf.goldMarginal(seg.segments, seg.words).computeSpanConstraints()
      } else {
        orig
      }
    }


    def scorer(w: IndexedSeq[W]): Anchoring[L, W] = {
      val c = constraints(w)

      new Anchoring[L, W] {
        def words: IndexedSeq[W] = w


        def constraints: LabeledSpanConstraints[L] = c

        def labelIndex: OptionIndex[L] = crf.labelIndex

        def scoreTransition(prev: Int, cur: Int, begin: Int, end: Int): Double =
          numerics.logI(c.isAllowedLabeledSpan(begin, end, cur))

      }

    }
  }


  trait IndexedFeaturizer[L, W] {
    def anchor(w: IndexedSeq[W]): AnchoredFeaturizer[L, W]


    def labelIndex: OptionIndex[L]

    def featureIndex: Index[Feature]

    def hasTransitionFeatures: Boolean = true
  }

  trait AnchoredFeaturizer[L, W] {
    def featureIndex: Index[Feature]

    def featuresForTransition(prev: Int, cur: Int, begin: Int, end: Int): FeatureVector
  }


  def viterbi[L, W](anchoring: Anchoring[L, W], id: String = ""): Segmentation[L, W] = {
    val length = anchoring.length
    val numLabels = anchoring.labelIndex.size
    // total weight (logSum) for ending in pos with label l.
    val forwardScores = Array.fill(length + 1, numLabels)(Double.NegativeInfinity)
    val forwardLabelPointers = Array.fill(length + 1, numLabels)(-1)
    val forwardBeginPointers = Array.fill(length + 1, numLabels)(-1)
    forwardScores(0)(anchoring.labelIndex(None)) = 0.0

    var end = 1
    while (end <= length) {
      var label = 0
      while (label < numLabels) {

        var begin = math.max(end - anchoring.maxSegmentLength(label), 0)

        while (begin < end) {
          if (anchoring.constraints.isAllowedLabeledSpan(begin, end, label)) {
            var prevLabel = 0
            while (prevLabel < numLabels) {
              val prevScore = forwardScores(begin)(prevLabel)
              if (prevScore != Double.NegativeInfinity) {
                val score = anchoring.scoreTransition(prevLabel, label, begin, end) + prevScore
                if (score > forwardScores(end)(label)) {
                  forwardScores(end)(label) = score
                  forwardLabelPointers(end)(label) = prevLabel
                  forwardBeginPointers(end)(label) = begin
                }
              }

              prevLabel += 1
            }
          }
          begin += 1
        }
        label += 1
      }

      end += 1
    }
    val segments = ArrayBuffer[(L, Span)]()
    def rec(end: Int, label: Int) {
      if (end != 0) {
        val bestStart = forwardBeginPointers(end)(label)
        anchoring.labelIndex.get(label).foreach { l =>
          segments += (l -> Span(bestStart, end))
        }
        rec(bestStart, forwardLabelPointers(end)(label))
      }

    }
    rec(length, (0 until numLabels).maxBy(forwardScores(length)(_)))

    Segmentation(segments.reverse, anchoring.words, id)
  }

  /** This function is the one to be alterred to find all posteriors
    * Should also take in an array of labes (array of arrays)
    * It gives back posteriors in the same order as the labels
    * Posteriors calculated as score below (with mult), going though each label
    * The score values are then normalized over all labels.
    *
    * @param m
    * @param id
    * @tparam L
    * @tparam W
    * @return
    */
  def posteriorDecode[L, W](m: Marginal[L, W], id: String = "") = {
    val length = m.length
    val numLabels = m.anchoring.labelIndex.size
    val forwardScores = Array.fill(length + 1, numLabels)(0.0)
    val forwardLabelPointers = Array.fill(length + 1, numLabels)(-1)
    val forwardBeginPointers = Array.fill(length + 1, numLabels)(-1)
    forwardScores(0)(m.anchoring.labelIndex(None)) = 1.0

    var end = 1
    while (end <= length) {
      var label = 0
      while (label < numLabels) {
        var begin = math.max(end - m.anchoring.maxSegmentLength(label), 0)
        while (begin < end) {
          var prevLabel = 0
          while (prevLabel < numLabels) {
            val prevScore = forwardScores(begin)(prevLabel)
            if (prevScore != 0.0) {
              val score = m.transitionMarginal(prevLabel, label, begin, end) + prevScore //Should not be added, but multiplied for CRF
              // println("TransitionMarginals: " + m.transitionMarginal(prevLabel, label, begin, end))
              if (score > forwardScores(end)(label)) {
                // He makes numLabels^2 calc. for each label, ignores previous seq
                forwardScores(end)(label) = score
                forwardLabelPointers(end)(label) = prevLabel
                forwardBeginPointers(end)(label) = begin
              }
            }

            prevLabel += 1
          }
          begin += 1
        }
        label += 1
      }

      end += 1
    }
    val segments = ArrayBuffer[(L, Span)]()
    def rec(end: Int, label: Int) {
      if (end != 0) {
        val bestStart = forwardBeginPointers(end)(label)
        m.anchoring.labelIndex.get(label).foreach { l =>
          segments += (l -> Span(bestStart, end))
        }

        rec(bestStart, forwardLabelPointers(end)(label))
      }

    }
    //println("ForwardBeginPointers: " + forwardBeginPointers.deep.mkString("\n"))
    rec(length, (0 until numLabels).maxBy(forwardScores(length)(_)))
    //println("Forwardscore: " + forwardScores.deep.mkString("\n"))
    //println("Segments: " +segments)
    Segmentation(segments.reverse, m.words, id)
  }


  case class ProductAnchoring[L, W](a: Anchoring[L, W], b: Anchoring[L, W]) extends Anchoring[L, W] {
    if ((a.labelIndex ne b.labelIndex) && (a.labelIndex != b.labelIndex)) throw new IllegalArgumentException("Elements of product anchoring must have the same labelIndex!")

    def words: IndexedSeq[W] = a.words

    val constraints: LabeledSpanConstraints[L] = a.constraints & b.constraints

    def scoreTransition(prev: Int, cur: Int, begin: Int, end: Int): Double = {
      var score = a.scoreTransition(prev, cur, begin, end)
      if (score != Double.NegativeInfinity) {
        score += b.scoreTransition(prev, cur, begin, end)
      }
      score
    }

    def labelIndex = a.labelIndex
  }

  class IdentityAnchoring[L, W](val words: IndexedSeq[W], val labelIndex: OptionIndex[L], val constraints: LabeledSpanConstraints[L]) extends Anchoring[L, W] {
    def scoreTransition(prev: Int, cur: Int, beg: Int, end: Int): Double = 0.0

    def canStartLongSegment(pos: Int): Boolean = true
  }

  /** Here be dragons. Här startar våra kodgrejer
    *
    *
    *
    *
    *
    *
    *
    *
    *
    */

  /**
    * Makes a set of labels, maximum 5 malwares per label, maximum N labels
    * Returns array of array (OR WILL DUNDUNDUUUUUN)
    *
    * @param m
    * @tparam L
    * @tparam W
    */
  def makeLabels[L, W](m: Marginal[L, W], bestScore: Double): ArrayBuffer[Array[Int]] = {
    var r = Random
    val length = m.length
    var N = 500.0 //totalNumLabel
    //if (2*length < N) {
     // N = 2*length
    //}

    val percentageMax = 0.1
    val sisterLabel = 1
    var labels = new ArrayBuffer[Array[Int]]
    var counter = 0

    var label = Array.fill(length)(2) // No malware label
    labels += label
    var labelScore = 0.0
    var numOfSisters = 0
    var nAllSisters = 0
    var numMal = 0
    var malwareIndex = Array[Int]()
    var randomIndex = 0
    var sisters = ArrayBuffer[Array[Int]]()
    var alreadyContains = false
    /*for (i <- 0 until length) {
      label = Array.fill(length)(2) // No malware label
      label(i) = 0; // Fill in one label
      labels += label
      val sisters = getSisters(label, Array(i + 1), bicoSum(1), sisterLabel, percentageMax)
      labels = labels ++ sisters
      numOfSisters = sisters.size
      nAllSisters += numOfSisters
    }*/
    //N = N - length - nAllSisters
    val numOfLabels = Array(0, 0.7287 * N, 0.204 * N, 0.05355 * N, 0.01071 * N, 0.00153 * N, 0.00051 * N, 0.00051 * N, 0.00051 * N) // One-mal already created
    // Labels: 0 = B_MAL, 1 = I_MAL, 2 = None
    var maxMal = 9
    if (length < maxMal){
      maxMal = length
    }


    //for (numMal <- 2 to numOfLabels.length) { // Starts at two, since all one-malware labels already created
    //if(numMal<=length/2) {s
    var currentNumOfLabels = 0
    while (currentNumOfLabels < N && counter < 1000*length) {
      //numOfLabels(numMal - 1)) {
      numMal = r.nextInt(maxMal) + 1
      counter += 1
      malwareIndex = Array[Int](numMal)
      //Create original label
      malwareIndex = Array.fill(numMal)(0)
      label = Array.fill(length)(2)
      for (addMal <- 1 to numMal) {
        randomIndex = r.nextInt(length) + 1
        while (malwareIndex contains randomIndex) {
          randomIndex = r.nextInt(length) + 1
        }
        malwareIndex(addMal - 1) = randomIndex
        label(randomIndex - 1) = 0
      }
      // Create all sisters
      sisters = getSisters(label, malwareIndex, bicoSum(numMal), sisterLabel, percentageMax)
      labelScore = bestLabelScore(m, Array(label))(0)
      //println("LabelScore: "+ labelScore)
      alreadyContains = false
      breakable {
        for (i <- labels.indices) {
          if ((labels(i): Seq[Int]) == (label: Seq[Int])) {
            alreadyContains = true
            break
          }
        }
      }
      if ((labelScore > r.nextDouble()) && !alreadyContains) {//r.nextDouble() < 10 * labelScore / bestScore
        //println(label.mkString(" "))
        labels += label
        currentNumOfLabels += 1
      }
      for (i <- sisters.indices) {
        alreadyContains = false
        breakable {
          for (j <- labels.indices) {
            if ((labels(j): Seq[Int]) == (sisters(i): Seq[Int])) {
              alreadyContains = true
              break
            }
          }
        }
        labelScore = bestLabelScore(m, Array(sisters(i)))(0)
        if ((labelScore>r.nextDouble()) && !(labels.toArray contains sisters(i))) {
          labels += sisters(i)
          currentNumOfLabels += 1
        }
      }
    }

    //labels += Array.fill(length)(1)
    return labels

  }

  /** Gets a sequence label, selects a sister with 0.05 percent, and return selected sister(s), if any
    *
    * @param label
    * @param indices
    * @param amount
    * @param sisterLabel
    * @param percentageMax
    * @return
    */
  def getSisters(label: Array[Int], indices: Array[Int], amount: Int, sisterLabel: Int, percentageMax: Double): ArrayBuffer[Array[Int]] = {
    var i = 0
    val numMal = indices.length
    val binSist = getBinSist(numMal, percentageMax)

    val sisters = placeSisters(binSist, indices, label, sisterLabel)

    return sisters
  }

  /** Returns sisters in binomial form (i.e add or not add inner label represented by 0 or 1)
    * Percentage normalized by numMal, so that, as a sentence with more malware has more possible sisters,
    * one sister per label is still selected at percentageMax rate.
    *
    * @param numMal
    * @param percentageMax
    * @return
    */
  def getBinSist(numMal: Int, percentageMax: Double): Array[Array[Int]] = {
    var tmp = 0
    var binString = ""
    var possibleSist = new ArrayBuffer[Array[Int]]()
    val percentage = percentageMax / numMal.toDouble

    for (tmp <- 1 until Math.pow(2, numMal).toInt) {
      val r = Random
      val rand = r.nextDouble()
      if (rand < percentage) {
        binString = tmp.toBinaryString
        for (addZeros <- 1 to numMal - binString.length) {
          binString = "0" + binString
        }
        val numSisters = binString.count(_ == '1')
        if (numSisters == 1 || (numSisters > 1 && rand < Math.pow(percentage, numSisters))) {
          //System.out.println("numSisters is " +numSisters + " and rand is " + Math.pow(rand,numSisters.toDouble))
          possibleSist += binString.toCharArray.map(_.toString).map(_.toInt)
        }
      }
    }
    val binSist = possibleSist.toArray
    /////println(binSist.deep.mkString("\n"))
    return binSist

  }

  /** From a list of binomial possible sisters, placeSisters checks if these are possible (no index out of bounds f.ex)
    * Then creates the sister from the label, with inner labeles added accordingly
    *
    * @param possibleSist
    * @param indices
    * @param label
    * @param sisterLabel
    * @return
    */
  def placeSisters(possibleSist: Array[Array[Int]], indices: Array[Int], label: Array[Int], sisterLabel: Int): ArrayBuffer[Array[Int]] = {
    var i = 0
    var tmpLabel = label.clone()
    var sisters = new ArrayBuffer[Array[Int]]
    val numMal = indices.length
    for (i <- 0 until possibleSist.length) {
      var j = 0
      var count = 0
      var tmpArray = Array[Int]()
      for (j <- 0 until numMal) {
        if (possibleSist(i)(j) == 1 && indices(j) != label.length) {
          //1 for adding sister
          if (tmpLabel(indices(j)) != 0) //0 for malware label
          {
            if (count == 0) {
              tmpArray = tmpLabel.clone()
              count = 1
            }
            tmpArray(indices(j)) = sisterLabel
          }
        }
      }
      var same = false
      if (tmpArray.length != 0) {
        var x = 0
        if (sisters.size != 0) {

          for (x <- 0 until sisters.size) {
            if (sisters(x).deep == tmpArray.deep && !same) {
              same = true
            }

          }
          if (!same) {
            sisters += tmpArray.clone()
          }
        }
        else {
          sisters += tmpArray.clone()
        }
      }
    }
    return sisters

  }

  /**
    * Returns number of all possible labels of a sentence with n words (Max 5 mal per sent)
    *
    * @param n
    * @return
    */
  def possibleLabels(n: Int): Int = {
    var posLab = 0
    var i = 1
    for (i <- 1 to 5) {
      if (i <= n) {
        posLab += bico(n, i)
      }
    }
    return posLab
  }

  /**
    * Binomial calculations
    *
    * @param n
    * @param k
    * @return
    */
  def bico(n: Int, k: Int): Int = (n, k) match {
    case (n, 0) => 1
    case (0, k) => 0
    case (n, k) => bico(n - 1, k - 1) + bico(n - 1, k)
  }

  def bicoSum(x: Int): Int = {
    var sum = 0
    var i = 0
    for (i <- 2 to x + 1) {
      sum = sum + bico(x, i - 1)
    }
    return sum
  }


  def bestLabelScore[L, W](m: Marginal[L, W], labels: Array[Array[Int]]): Array[Double] = {
    val length = m.length
    val nOfLabels = labels.size
    var labelIter = 0
    var scoreArray = Array.fill(nOfLabels)(0.0) // Make array
    val numLabels = m.anchoring.labelIndex.size
    var label = 0
    var prevLabel = 0
    var scoreSum = 0.0
    for (labelIter <- 0 until nOfLabels) {
      var score = 1.0
      var position = 1
      for (position <- 1 until length) {
        label = labels(labelIter)(position)
        prevLabel = labels(labelIter)(position - 1)
        score = m.transitionMarginal(prevLabel, label, position, position + 1) + score // !!! + -> *
      }
      scoreSum += score/length // !!! score/length -> score
      scoreArray(labelIter) = score/length
    }
    var i = 0

    if (scoreSum != 0 && nOfLabels > 1) {
      for (i <- 0 until nOfLabels) {
        scoreArray(i) = scoreArray(i) / scoreSum
      }
    }
    //println(scoreArray.mkString(" "))
    val maxV = scoreArray.reduceLeft(_ max _)
    val index = scoreArray.indexWhere(_ == maxV)
    //println("Best score is " + maxV + " at index " + index + " of " + nOfLabels + " labels")
    return scoreArray
  }

  /**
    * LeastConfidence, does the same as posteriorDecode, but returns only the score value, not the label
    *
    * @param m
    * @tparam L
    * @tparam W
    * @return
    */
  def getBestScore[L, W](m: Marginal[L, W]): Double = {
    val length = m.length
    val numLabels = m.anchoring.labelIndex.size
    //println("numLabels is "+numLabels)
    val forwardScores = Array.fill(length + 1, numLabels)(0.0)
    forwardScores(0)(m.anchoring.labelIndex(None)) = 1.0

    var end = 1
    while (end <= length) {
      var label = 0
      while (label < numLabels) {
        var begin = math.max(end - m.anchoring.maxSegmentLength(label), 0)
        while (begin < end) {
          var prevLabel = 0
          while (prevLabel < numLabels) {
            val prevScore = forwardScores(begin)(prevLabel)
            if (prevScore != 0.0) {
              val score = m.transitionMarginal(prevLabel, label, begin, end) * prevScore //SHould not be added, but multiplied for CRF
              // println("TransitionMarginals: " + m.transitionMarginal(prevLabel, label, begin, end))
              if (score > forwardScores(end)(label)) {
                // He makes numLabels^2 calc. for each label, ignores previous seq
                forwardScores(end)(label) = score
              }
            }

            prevLabel += 1
          }
          begin += 1
        }
        label += 1
      }

      end += 1
    }
    //println(forwardScores.last.reduceLeft(_ max _))
    forwardScores.last.reduceLeft(_ max _)
  }

}

