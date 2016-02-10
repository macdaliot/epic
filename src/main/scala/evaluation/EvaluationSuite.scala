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
    System.out.println("bananaMore")
  }

  def classify(s : String) : TaggedSequence[Option[String], String] = {
    // ** Construct Epic classifier **
    // read model file from disk
    val modelFile = new File("./data/en_malware.ser.gz")
    // instantiate SemiCRF from model file
    val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)

    // ** Preprocess sentence **
    val words = s.split(" ").toSeq

    System.out.println(model.marginal(words.to))

    System.out.println("banana")
    model.bestSequence(words.to).asFlatTaggedSequence


    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar evaluation.EvaluationSuite "Stuxnet is a malware"
  }

}