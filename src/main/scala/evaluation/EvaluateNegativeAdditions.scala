package evaluation

import java.io.File
import java.io._
import epic.sequences.SemiCRF
import epic.sequences.SemiConllNerPipeline
import scala.util.control.Breaks._

/**
  * Created by elin on 23/03/16.
  */

object EvaluateNegativeAdditions {

  def main(args: Array[String]): Unit = {
    val modelFileName: String = "./data/Possibly_unnecessary/en_malware.ser.gz"
    val modelFile: File = new File(modelFileName)
    var model: SemiCRF[String, String] = breeze.util.readObject(modelFile)
    val testFileName:  String = "./data/epicEvalutationTestSet/epicEvalutationTestSet.conll"
    val posFileName: String = "./data/epicEvalutationTestSet/positives.conll"
    val negFileName: String = "./data/epicEvalutationTestSet/negatives.conll"
    val fakePosFileName: String = "./data/epicEvalutationTestSet/fakePositives.conll"
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
    println("******** All Variables set up *********")

    for (file <- new File("./data/EvaluateNegativeAdditionsDataSets").listFiles) {
      counter +=1
      if (counter > 11){
        allPW.close()
        posPW.close()
        negPW.close()
        posFakePW.close()
        break
      }
      println("\n")
      println("******** Folder Opened *********")
      val trainingString: Array[String] = Array("--train", "data/EvaluateNegativeAdditionsDataSets/dataSet" + counter + ".conll",
      "--test", "data/epicEvaluationTestSet/epicEvalutationTestSet.conll", "--modelOut", modelFileName,
        "--useStochastic", "false", "--regularization", "1")
      println("File "+ counter+ " has training string " + trainingString.mkString(" "))
      SemiConllNerPipeline.main(trainingString)
      model = breeze.util.readObject(modelFile)
      val stats: Array[Double] = EpicEvaluation.evaluateModel(testFile, model,true)
      val statsPos: Array[Double] = EpicEvaluation.evaluateModel(posFile, model,true)
      val statsNeg: Array[Double] = EpicEvaluation.evaluateModel(negFile, model,true)
      val statsFakePos: Array[Double] = EpicEvaluation.evaluateModel(fakePosFile, model,true)

      println("Stats: " + stats.mkString(" "))
      println("Stats pos: " + statsPos.mkString(" "))
      println("Stats neg: " + statsNeg.mkString(" "))
      println("Stats fake pos: " + statsFakePos.mkString(" "))
      var precisionAll = stats(2)/(stats(2)+stats(0))
      var recallAll = stats(2)/(stats(2)+stats(1))
      var F1All = 2*precisionAll*recallAll/(precisionAll+recallAll)
      println("F1: "+F1All+ " Precision: "+precisionAll+" Recall: "+recallAll)
      allPW.write(precisionAll + " " + recallAll +  " " + F1All + "\n")
      precisionAll = statsPos(2)/(statsPos(2)+statsPos(0))
      recallAll = statsPos(2)/(statsPos(2)+statsPos(1))
      F1All = 2*precisionAll*recallAll/(precisionAll+recallAll)
      println("F1: "+F1All+ " Precision: "+precisionAll+" Recall: "+recallAll)
      posPW.write(precisionAll + " " + recallAll +  " " + F1All + "\n")
      negPW.write(stats.mkString(" ")+ "\n")
      precisionAll = statsFakePos(2)/(statsFakePos(2)+statsFakePos(0))
      recallAll = statsFakePos(2)/(statsFakePos(2)+statsFakePos(1))
      F1All = 2*precisionAll*recallAll/(precisionAll+recallAll)
      println("F1: "+F1All+ " Precision: "+precisionAll+" Recall: "+recallAll)
      posFakePW.write(precisionAll + " " + recallAll +  " " + F1All + "\n")

    }



  }
}
