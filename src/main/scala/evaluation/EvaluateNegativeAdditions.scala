package evaluation

import java.io.File

import epic.sequences.SemiCRF
import epic.sequences.SemiConllNerPipeline

/**
  * Created by elin on 23/03/16.
  */

object EvaluateNegativeAdditions {

  def main(args: Array[String]): Unit = {
    val modelFileName: String = "./data/our_malware.ser.gz"
    val modelFile: File = new File(modelFileName)
    val model: SemiCRF[String, String] = breeze.util.readObject(modelFile)
    val testFileName:  String = "./data/epicEvalutationTestSet"
    val posFileName: String = "./data/positiveTestSet"
    val negFileName: String = "./data/negativeTestSet"
    val fakePosFileName: String = "./data/fakePositiveTestSet"
    val testFile: File = new File(testFileName)
    val posFile: File = new File(posFileName)
    val negFile: File = new File(negFileName)
    val fakePosFile: File = new File(fakePosFileName)
    val folderOfData: String = ""
    var counter: Int = 0
    for (file <- new File("./data/EvaluateNegativeAdditionsDataSets").listFiles) {
      counter +=1
      val trainingString: Array[String] = Array("--train", "data/EvaluateNegativeAdditionsDataSets/dataSet" + counter + ".conll",
      "--test", "data/epicEvalutationTestSet.conll", "--modelOut", modelFileName,
        "--useStochastic", "false", "--regularization", "1")
      SemiConllNerPipeline.main(trainingString)
      val statsPos: Array[Double] = EpicEvaluation.evaluateModel(posFile, model)
      val statsNeg: Array[Double] = EpicEvaluation.evaluateModel(negFile, model)
      val statsFakePos: Array[Double] = EpicEvaluation.evaluateModel(fakePosFile, model)
      val stats: Array[Double] = EpicEvaluation.evaluateModel(testFile, model)

      println("Stats: " + stats.mkString(" "))
      println("Stats pos: " + statsPos.mkString(" "))
      println("Stats neg: " + statsNeg.mkString(" "))
      println("Stats fake pos: " + statsFakePos.mkString(" "))
    }
  }
}
