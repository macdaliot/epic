package evaluation

import java.io.File

import epic.sequences.{SemiCRF, SemiConllNerPipeline}

/**
  * Created by elin on 23/03/16.
  */
public

object EvaluateNegativeAdditions {

  def main(args: Array[String]): Unit = {
    val modelFileName: String = "./data/our_malware.ser.gz"
    val modelFile: File = new File(modelFileName)
    val model: SemiCRF[String, String] = breeze.util.readObject(modelFile)
    val testFileName:  String = ""
    val testFile: File = new File(testFileName)
    val folderOfData: String = ""
    var counter: Int = 0
    for (file <- new File("./data/EvaluateNegativeAdditionsDataSets").listFiles) {
      counter +=1
      val trainingString: Array[String] = Array("--train", "data/EvaluateNegativeAdditionsDataSets/dataSet" + counter + ".conll",
      "--test", "data/epicEvalutationTestSet.conll", "--modelOut", modelFileName,
        "--useStochastic", "false", "--regularization", "1");
      SemiConllNerPipeline.main(trainingString)
      EpicEvaluation.evaluateModel(File, model)
    }
  }
}
