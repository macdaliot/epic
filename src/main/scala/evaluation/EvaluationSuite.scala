package evaluation

import java.io.File

import epic.sequences.{TaggedSequence, SemiCRF}


/**
  * Created by elin on 08/02/16.
  */
object EvaluationSuite {

  def main(args: Array[String]) : Unit = {
    val sentence : String = args(0)
    val taggedSequence = classify(sentence)
    System.out.println(taggedSequence)
  }

  def classify(s : String) : TaggedSequence[Option[String], String] = {
    // ** Construct Epic classifier **
    // read model file from disk
    val modelFile = new File("./data/en_malware.ser.gz")
    // instantiate SemiCRF from model file
    val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)

    // ** Preprocess sentence **
    val words = s.split(" ").toSeq

    model.bestSequence(words.to).asFlatTaggedSequence
  }

}
