package epic.parser
package projections

/*
 Copyright 2012 David Hall

 Licensed under the Apache License, Version 2.0 (the "License")
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/
import breeze.collection.mutable.TriangularArray
import breeze.config.{Help, Configuration}
import breeze.util.Index
import collection.immutable.BitSet
import java.io._
import ConstraintAnchoring.RawConstraints
import epic.trees._
import collection.mutable.ArrayBuffer
import breeze.stats.distributions.{Rand, Binomial}
import projections.ConstraintCoreGrammar.PruningStatistics
import actors.threadpool.Arrays
import breeze.linalg.DenseVector
import epic.parser.ParseChart.SparsityPattern

/**
 * 
 * @author dlwh
 */
@SerialVersionUID(2L)
class ConstraintAnchoring[L, W](val grammar: BaseGrammar[L],
                             val lexicon: Lexicon[L, W],
                             val words: Seq[W],
                             scores: Array[BitSet],
                             topScores: Array[BitSet],
                             override val sparsityPattern: SparsityPattern) extends CoreAnchoring[L, W] with Serializable {
  def scoreBinaryRule(begin: Int, split: Int, end: Int, rule: Int) = 0.0

  def scoreUnaryRule(begin: Int, end: Int, rule: Int) = {
    if (topScores eq null) 0.0
    else {
      val set = topScores(TriangularArray.index(begin, end))
      if(set == null || !set.contains(rule)) Double.NegativeInfinity
      else 0.0
    }
  }

  def scoreSpan(begin: Int, end: Int, tag: Int) = {
    if(scores eq null) 0.0
    else {
      val set = scores(TriangularArray.index(begin, end))
      if(set == null || !set.contains(tag)) Double.NegativeInfinity
      else 0.0
    }
  }
}

object ConstraintAnchoring {
  @SerialVersionUID(2)
  case class RawConstraints(bottom: Array[BitSet], top: Array[BitSet], sparsity: ParseChart.SparsityPattern) {
    def toAnchoring[L, W](grammar: BaseGrammar[L], lexicon: Lexicon[L, W], words: Seq[W]) = {
      new ConstraintAnchoring(grammar, lexicon, words, bottom, top, sparsity)
    }
  }
}

/**
 * Creates labeled span scorers for a set of trees from some parser.
 * @author dlwh
 */
class ConstraintCoreGrammar[L, W](augmentedGrammar: AugmentedGrammar[L, W], threshold: Double, viterbi: Boolean) extends CoreGrammar[L, W] {
  def grammar = augmentedGrammar.grammar
  def lexicon = augmentedGrammar.lexicon


  def anchor(words: Seq[W]): ConstraintAnchoring[L, W] = {
    val chartScorer = this.buildConstraints(words, GoldTagPolicy.noGoldTags[L])
    chartScorer
  }

  def buildConstraints(charts: Marginal[L, W],
                  goldTags: GoldTagPolicy[L] = GoldTagPolicy.noGoldTags[L]):ConstraintAnchoring[L, W] = {

    val RawConstraints(label,unary, sparsity) = rawConstraints(charts, goldTags)

    new ConstraintAnchoring[L, W](charts.anchoring.grammar, charts.anchoring.lexicon, charts.anchoring.words, label, unary, sparsity)
  }

  def buildConstraints(words: Seq[W],
                       goldTags: GoldTagPolicy[L]):ConstraintAnchoring[L, W] = {

    val charts = ChartMarginal(augmentedGrammar, words, if(viterbi) ParseChart.viterbi else ParseChart.logProb)
    buildConstraints(charts, goldTags)
  }

  def rawConstraints(words: Seq[W], gold: GoldTagPolicy[L]):RawConstraints = {
    val charts = ChartMarginal(augmentedGrammar, words, if(viterbi) ParseChart.viterbi else ParseChart.logProb)
    rawConstraints(charts, gold)
  }

  def rawConstraints(marg: Marginal[L, W], gold: GoldTagPolicy[L]): RawConstraints = {
    val length = marg.length
    val (botLabelScores, unaryScores) = computeScores(length, marg)

    val labelThresholds = extractLabelThresholds(length,
                                                 grammar.labelIndex.size,
                                                 botLabelScores,
                                                 gold.isGoldBotTag(_, _, _))
    val unaryThresholds = extractLabelThresholds(length,
                                                 grammar.index.size,
                                                 unaryScores,
                                                 { (i, j, r) => gold.isGoldTopTag(i, j, grammar.parent(r))})

    val topLabelThresholds = unaryThresholds.map { rules =>
      if(rules == null) null else rules.map(r => grammar.parent(r))
    }

    val pattern = ConstraintCoreGrammar.ConstraintSparsity(labelThresholds, topLabelThresholds)

    RawConstraints(labelThresholds, unaryThresholds, pattern)
  }


  private def extractLabelThresholds(length: Int, numLabels: Int,
                                     scores: Array[Array[Double]],
                                     isGold: (Int, Int, Int)=>Boolean): Array[BitSet] = {
    TriangularArray.tabulate[BitSet](length + 1) { (i, j) =>
        val arr = scores(TriangularArray.index(i, j))
        val thresholdedTags = if (arr eq null) {
          BitSet.empty
        } else BitSet.empty ++ (0 until arr.length filter { s =>
          math.log(arr(s)) > threshold
        })
        val goldTags = (0 until numLabels).filter { isGold(i, j, _) }
        val result = thresholdedTags ++ goldTags
        if (result.nonEmpty) result
        else null
    }.data
  }

  def computePruningStatistics(words: Seq[W], gold: GoldTagPolicy[L]): (PruningStatistics, PruningStatistics) = {
    val charts = ChartMarginal(augmentedGrammar, words, if(viterbi) ParseChart.viterbi else ParseChart.logProb)
    computePruningStatistics(charts, gold)
  }

  def computePruningStatistics(marg: Marginal[L, W], gold: GoldTagPolicy[L]): (PruningStatistics, PruningStatistics) = {
    val counts = DenseVector.zeros[Double](grammar.labelIndex.size)
    val (scores, topScores) = computeScores(marg.length, marg)
    var nConstructed = 0
    val thresholds = ArrayBuffer[Double]()
    var nGoldConstructed = 0
    val gThresholds = ArrayBuffer[Double]()
    for(i <-  0 until marg.length; j <- (i+2) to marg.length) {
      {
      val arr = scores(TriangularArray.index(i, j))
      if (arr ne null)
        for(c <- 0 until grammar.labelIndex.size) {
          thresholds += arr(c)
          nConstructed += 1
          if(gold.isGoldBotTag(i, j, c)) {
            if(arr(c) != 0)
              nGoldConstructed += 1
            else counts(c) += 1
            gThresholds += arr(c)
          }
       } }
      /*{
      val arr = topScores(TriangularArray.index(i, j))
      if (arr ne null)
        for(c <- 0 until grammar.labelIndex.size) {
          thresholds += arr(c)
          nConstructed += 1
          if(gold.isGoldTopTag(i, j, c)) {
            if(arr(c) != 0)
              nGoldConstructed += 1
            else counts(c) += 1
            gThresholds += arr(c)
          }
       } }*/
    }

    import ConstraintCoreGrammar._
    PruningStatistics(thresholds.toArray, nConstructed, counts) -> PruningStatistics(gThresholds.toArray, nGoldConstructed, counts)
  }


  private def computeScores(length: Int, marg: Marginal[L, W]) = {
    val scores = TriangularArray.raw(length + 1, null: Array[Double])
    val topScores = TriangularArray.raw(length + 1, null: Array[Double])
    val visitor = new AnchoredVisitor[L] {
      def visitBinaryRule(begin: Int, split: Int, end: Int, rule: Int, ref: Int, score: Double) {}

      def visitUnaryRule(begin: Int, end: Int, rule: Int, ref: Int, score: Double) {
        val index = TriangularArray.index(begin, end)
        if (score != 0.0) {
          if (topScores(index) eq null) {
            topScores(index) = new Array[Double](grammar.index.size)
          }
          topScores(index)(rule) += score
        }
      }


      def visitSpan(begin: Int, end: Int, tag: Int, ref: Int, score: Double) {
        val index = TriangularArray.index(begin, end)
        if (score != 0.0) {
          if (scores(index) eq null) {
            scores(index) = new Array[Double](grammar.labelIndex.size)
          }
          scores(index)(tag) += score
        }
      }
    }

    marg.visit(visitor)
    (scores,topScores)
  }
}

object ConstraintCoreGrammar {

  case class PruningStatistics(data: Array[Double], nConstructed: Double, pruningCounts: DenseVector[Double]) {
    def merge(other: PruningStatistics, nAllowed:Int = data.length): PruningStatistics = {
      if(nAllowed >= data.length + other.data.length) {
        PruningStatistics(data ++ other.data, this.nConstructed + other.nConstructed, pruningCounts + other.pruningCounts)
      } else {
        val subsetThisSize = new Binomial(nAllowed, nConstructed/(other.nConstructed + nConstructed)).draw()
        val subset1 = Rand.subsetsOfSize(data, subsetThisSize).draw()
        val subset2 = Rand.subsetsOfSize(data, nAllowed - subsetThisSize).draw()
        PruningStatistics(subset1 ++ subset2 toArray, this.nConstructed + other.nConstructed, pruningCounts + other.pruningCounts)
      }
    }
  }

  object PruningStatistics {
    def empty(nsyms: Int) = PruningStatistics(Array.empty, 0, DenseVector.zeros(nsyms))
  }

  @SerialVersionUID(1L)
  private case class ConstraintSparsity(bot: Array[BitSet], top: Array[BitSet]) extends SparsityPattern with Serializable {
    val activeTriangularIndices: BitSet = BitSet.empty ++ (0 until bot.length).filter{ i =>
      (bot(i) != null && bot(i).nonEmpty) || (top(i) != null && top(i).nonEmpty)
    }

    def activeLabelsTop(begin: Int, end: Int): BitSet = {
      val r = top(TriangularArray.index(begin, end))
      if(r == null) BitSet.empty
      else r
    }
    def activeLabelsBot(begin: Int, end: Int): BitSet = {
      val r = bot(TriangularArray.index(begin, end))
      if(r == null) BitSet.empty
      else r
    }
  }

}


case class ProjectionParams(treebank: ProcessedTreebank,
                            @Help(text="Location of the parser")
                            parser: File,
                            @Help(text="Where to save constraints")
                            out: File = new File("constraints.ser.gz"),
                            @Help(text="Longest train sentence to build constraints for.")
                            maxParseLength: Int = 80,
                            threshold: Double = -5,
                            viterbi: Boolean = true) {
}

object ProjectTreebankToConstraints {

  def main(args: Array[String]) {
    val (baseConfig, files) = breeze.config.CommandLineParser.parseArguments(args)
    val config = baseConfig backoff Configuration.fromPropertiesFiles(files.map(new File(_)))
    val params = config.readIn[ProjectionParams]("")
    val treebank = params.treebank.copy(maxLength = 1000000)
    println(params)
    val parser = loadParser[Any](params.parser)

    val out = params.out
    out.getAbsoluteFile.getParentFile.mkdirs()

    val factory = new ConstraintCoreGrammar[AnnotatedLabel, String](parser.augmentedGrammar, params.threshold, params.viterbi)
    val train = mapTrees(factory, treebank.trainTrees, parser.grammar.labelIndex, useTree = true, maxL = params.maxParseLength)
    val test = mapTrees(factory, treebank.testTrees, parser.grammar.labelIndex, useTree = false, maxL = 10000)
    val dev = mapTrees(factory, treebank.devTrees, parser.grammar.labelIndex, useTree = false, maxL = 10000)
    val map: Map[Seq[String], RawConstraints] = Map.empty ++ train ++ test ++ dev
    breeze.util.writeObject(out, map)
  }

  def loadParser[T](loc: File): SimpleChartParser[AnnotatedLabel, String] = {
    val parser = breeze.util.readObject[SimpleChartParser[AnnotatedLabel, String]](loc)
    parser
  }

  def mapTrees(factory: ConstraintCoreGrammar[AnnotatedLabel, String],
               trees: IndexedSeq[TreeInstance[AnnotatedLabel, String]],
               index: Index[AnnotatedLabel],
               useTree: Boolean, maxL: Int): Seq[(Seq[String], RawConstraints)] = {
    trees.toIndexedSeq.par.flatMap { (ti:TreeInstance[AnnotatedLabel, String]) =>
      val TreeInstance(id, tree, words) = ti
      println(id, words)
      if(words.length > maxL) {
        Seq.empty
      } else  try {
        val policy = if(useTree) {
          GoldTagPolicy.goldTreeForcing[AnnotatedLabel](tree.map(_.baseAnnotatedLabel).map(index))
        } else {
          GoldTagPolicy.noGoldTags[AnnotatedLabel]
        }
        val scorer = factory.rawConstraints(words, policy)
        Seq(words -> scorer)
      } catch {
        case e: Exception => e.printStackTrace()
        Seq.empty[(Seq[String],RawConstraints)]
      }
    }.seq
  }


}


/**
 * Computes a CDF for how many labels are pruned at different levels of pruning.
 *
 * @author dlwh
 */
object ComputePruningThresholds {

  def main(args: Array[String]) {
    val (baseConfig, files) = breeze.config.CommandLineParser.parseArguments(args)
    val config = baseConfig backoff Configuration.fromPropertiesFiles(files.map(new File(_)))
    val params = config.readIn[ProjectionParams]("")
    val treebank = params.treebank.copy(maxLength = 1000000)
    println(params)
    val parser = loadParser[Any](params.parser)

    val factory = new ConstraintCoreGrammar[AnnotatedLabel, String](parser.augmentedGrammar, -7, params.viterbi)
    val (all, gold) = mapTrees(factory, treebank.devTrees, parser.grammar.labelIndex)
    Arrays.sort(all.data)
    Arrays.sort(gold.data)
    val goldOut = new PrintStream(new BufferedOutputStream(new FileOutputStream("gold.txt")))
    gold.data foreach goldOut.println _
    goldOut.close()
    val allOut = new PrintStream(new BufferedOutputStream(new FileOutputStream("all.txt")))
    all.data foreach allOut.println _
    allOut.close()
    println(parser.grammar.labelEncoder.decode(gold.pruningCounts))
  }

  def loadParser[T](loc: File): SimpleChartParser[AnnotatedLabel, String] = {
    val parser = breeze.util.readObject[SimpleChartParser[AnnotatedLabel, String]](loc)
    parser
  }

  def mapTrees(factory: ConstraintCoreGrammar[AnnotatedLabel, String],
               trees: IndexedSeq[TreeInstance[AnnotatedLabel, String]],
               index: Index[AnnotatedLabel]): (PruningStatistics, PruningStatistics) = {
    trees.toIndexedSeq.par.aggregate((PruningStatistics.empty(factory.grammar.labelIndex.size), PruningStatistics.empty(factory.grammar.labelIndex.size)))({ (s: (PruningStatistics,PruningStatistics), ti:TreeInstance[AnnotatedLabel, String]) =>
      val TreeInstance(id, tree, words) = ti
      println(id, words)
      try {
        val policy = GoldTagPolicy.goldTreeForcing[AnnotatedLabel](tree.map(_.baseAnnotatedLabel).map(index))
        val (ra, rb) = factory.computePruningStatistics(words, policy)
        (s._1.merge(ra, 100000), s._2.merge(rb, 100000))
      } catch {
        case e: Exception => e.printStackTrace()
        s
      }
    }, { (statsA, statsB) =>
      (statsA._1.merge(statsB._1, 100000) -> statsA._2.merge(statsB._1,  100000))
    })
  }


}