package evaluation

import java.io.File
import java.io._
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
    val testFileName:  String = "./data/epicEvalutationTestSet.conll"
    val posFileName: String = "./data/positives.conll"
    val negFileName: String = "./data/negatives.conll"
    val fakePosFileName: String = "./data/fakePositives.conll"
    val testFile: File = new File(testFileName)
    val posFile: File = new File(posFileName)
    val negFile: File = new File(negFileName)
    val fakePosFile: File = new File(fakePosFileName)
    val folderOfData: String = ""
    var counter: Int = 0
    val allPW = new PrintWriter(new File("./data/statsAll.txt"))
    val posPW = new PrintWriter(new File("./data/statsPos.txt"))
    val negPW = new PrintWriter(new File("./data/statsNeg.txt"))
    val posFakePW = new PrintWriter(new File("./data/statsPosFake.txt"))

    for (file <- new File("./data/EvaluateNegativeAdditionsDataSets").listFiles) {
      counter +=1
      val trainingString: Array[String] = Array("--train", "data/EvaluateNegativeAdditionsDataSets/dataSet" + counter + ".conll",
      "--test", "data/epicEvalutationTestSet.conll", "--modelOut", modelFileName,
        "--useStochastic", "false", "--regularization", "1")
      SemiConllNerPipeline.main(trainingString)
      val stats: Array[Double] = EpicEvaluation.evaluateModel(testFile, model)
      val statsPos: Array[Double] = EpicEvaluation.evaluateModel(posFile, model)
      val statsNeg: Array[Double] = EpicEvaluation.evaluateModel(negFile, model)
      val statsFakePos: Array[Double] = EpicEvaluation.evaluateModel(fakePosFile, model)

      println("Stats: " + stats.mkString(" "))
      println("Stats pos: " + statsPos.mkString(" "))
      println("Stats neg: " + statsNeg.mkString(" "))
      println("Stats fake pos: " + statsFakePos.mkString(" "))
      allPW.write(stats.mkString(" "))
      posPW.write(statsPos.mkString(" "))
      negPW.write(statsNeg.mkString(" "))
      posFakePW.write(statsFakePos.mkString(" "))
    }


    allPW.close()
    posPW.close()
    negPW.close()
    posFakePW.close()
  }
}
