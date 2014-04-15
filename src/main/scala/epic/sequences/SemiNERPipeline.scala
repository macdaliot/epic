package epic.sequences

import java.io._
import breeze.config.{Configuration, CommandLineParser}
import epic.ontonotes.{NERType, ConllOntoReader}
import collection.mutable.ArrayBuffer
import breeze.linalg.DenseVector
import epic.framework.ModelObjective
import breeze.optimize._
import chalk.corpora.CONLLSequenceReader
import nak.data.Example
import breeze.util.Encoder
import epic.trees.Span
import breeze.optimize.FirstOrderMinimizer.OptParams
import breeze.util.Implicits._
import epic.util.CacheBroker
import com.typesafe.scalalogging.slf4j.Logging


/**
 *
 * @author dlwh
 */
object SemiNERPipeline extends Logging {

  case class Params(path: File,
                    modelOut: File = new File("ner.model.gz"),
                    implicit val cache: CacheBroker,
                    name: String = "eval/ner",
                    nfiles: Int = 100000,
                    iterPerEval: Int = 20,
                    nthreads: Int = -1,
                    opt: OptParams,
                    checkGradient: Boolean = false)

  def main(args: Array[String]) {
    val params = CommandLineParser.readIn[Params](args)
    logger.info("Command line arguments for recovery:\n" + Configuration.fromObject(params).toCommandLineString)
    val (train, test) = {
      val instances =  for {
        file <- params.path.listFiles take params.nfiles
        doc <- ConllOntoReader.readDocuments(file)
        s <- doc.sentences
      } yield s.nerSegmentation
      instances.splitAt(instances.length * 9 / 10)
    }

    val gazetteer =  None//Gazetteer.ner("en")

    // build feature Index
    val model = new SegmentationModelFactory(NERType.OutsideSentence, NERType.NotEntity, gazetteer = gazetteer)(params.cache).makeModel(train)
    val obj = new ModelObjective(model, train, params.nthreads)
    val cached = new CachedBatchDiffFunction(obj)
    if(params.checkGradient) {
      GradientTester.test(cached, obj.initialWeightVector(true), toString = {(x: Int) => model.featureIndex.get(x).toString})
    }

    def eval(state: FirstOrderMinimizer[DenseVector[Double], BatchDiffFunction[DenseVector[Double]]]#State) {
      val crf = model.extractCRF(state.x)
      println("Eval + " + (state.iter+1) + " " + SegmentationEval.eval(crf, test, NERType.NotEntity))
    }

    val finalState = params.opt.iterations(cached, obj.initialWeightVector(randomize=false)).tee(state => if((state.iter +1) % params.iterPerEval == 0) eval(state)).take(params.opt.maxIterations).last
    eval(finalState)

    breeze.util.writeObject(params.modelOut, model.extractCRF(finalState.x))

  }

}



object SemiConllNERPipeline extends Logging {

  def makeSegmentation(ex: Example[IndexedSeq[String],IndexedSeq[IndexedSeq[String]]]): Segmentation[String, String]  = {
    val labels = ex.label
    val words = ex.features.map(_ apply 0)
    assert(labels.length == words.length)
    val out = new ArrayBuffer[(String, Span)]()
    var start = labels.length
    var i = 0
    while(i < labels.length) {
      val l = labels(i)
      l(0) match {
        case 'O' =>
          if(start < i)
            out += (labels(start).replaceAll(".-","").intern -> Span(start, i))
          out += ("O".intern -> Span(i, i+1))
          start = i + 1
        case 'B' =>
          if(start < i)
            out += (labels(start).replaceAll(".-","").intern -> Span(start, i))
          start = i
        case 'I' =>
          if(start >= i) {
            start = i
          } else if(labels(start) != l){
            out += (labels(start).replaceAll(".-","").intern -> Span(start, i))
            start = i
          } // else, still in a field, do nothing.
        case _  =>
          sys.error("weird label?!?" + l)
      }

      i += 1
    }
    if(start < i)
      out += (labels(start).replaceAll(".-","").intern -> Span(start, i))

    assert(out.nonEmpty && out.last._2.end == words.length, out + " " + words + " " + labels)
    Segmentation(out, words, ex.id)
  }



  case class Params(path: File,
                    test: File,
                    cache: CacheBroker,
                    name: String = "eval/ner",
                    nsents: Int = 100000,
                    nthreads: Int = -1,
                    iterPerEval: Int = 20,
                    opt: OptParams)

  def main(args: Array[String]) {
    val params = CommandLineParser.readIn[Params](args)
    logger.info("Command line arguments for recovery:\n" + Configuration.fromObject(params).toCommandLineString)
    val (train,test) = {
      val standardTrain = CONLLSequenceReader.readTrain(new FileInputStream(params.path), params.path.getName).toIndexedSeq
      val standardTest = CONLLSequenceReader.readTrain(new FileInputStream(params.test), params.path.getName).toIndexedSeq

      standardTrain.take(params.nsents).map(makeSegmentation) -> standardTest.map(makeSegmentation)
    }


    // build feature Index
    implicit val broker = params.cache
    val model: SemiCRFModel[String, String] = new SegmentationModelFactory("##", "O"/*, gazetteer = Gazetteer.ner("en" )*/).makeModel(train)
    val obj = new ModelObjective(model, train, params.nthreads)
    val cached = new CachedBatchDiffFunction(obj)

    //    GradientTester.test(cached, obj.initialWeightVector(true), toString={(i: Int) => model.featureIndex.get(i).toString})

    //
    def eval(state: FirstOrderMinimizer[DenseVector[Double], BatchDiffFunction[DenseVector[Double]]]#State) = {
      val out = new PrintWriter(new BufferedOutputStream(new FileOutputStream("weights.txt")))
      Encoder.fromIndex(model.featureIndex).decode(state.x).iterator.toIndexedSeq.sortBy(-_._2.abs).takeWhile(_._2.abs > 1E-4) foreach {case (x, v) => out.println(v + "\t" + x)}
      val crf: SemiCRF[String, String] = model.extractCRF(state.x)
      println("Eval + " + (state.iter+1))
      val stats = SegmentationEval.eval(crf, test, "O")
      println("Final: " + stats)
      out.close()
      stats
    }

    val weights = params.opt.iterations(cached, obj.initialWeightVector(randomize=false)).tee(state => if((state.iter +1) % params.iterPerEval == 0) eval(state)).take(params.opt.maxIterations).last
    val stats = eval(weights)
    println(stats)


  }

}
