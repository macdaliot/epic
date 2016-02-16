package evaluation

import java.io.File

import epic.sequences.{SemiConllNerPipeline, TaggedSequence, SemiCRF, SemiNerPipeline}


/**
  * Created by elin on 08/02/16.
  */
object EvaluationSuite {

  def main(args: Array[String]) : Unit = {
    val sentence : String = args(0)
    SemiConllNerPipeline.main(Array("--train",
      "data/labeledPool.conll",
      "--test", "data/conllFileTest.conll",
      "--modelOut", "data/our_malware.ser.gz"))

    //val taggedSequence = classify(sentence)
    //System.out.println(taggedSequence)
    //System.out.println("bananaMore")
  }

  def classify(s : String) : TaggedSequence[Option[String], String] = {
    // ** Construct Epic classifier **
    // read model file from disk
    val modelFile = new File("./data/our_malware.ser.gz")
    // instantiate SemiCRF from model file
    val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)

    // ** Preprocess sentence **
    val words = s.split(" ").toSeq

    System.out.println(model.marginal(words.to))

    System.out.println("banana")
    print("getPosteriors: ")
    println(model.getPosteriors(words.to).mkString(" "))
    print("leastConfidence: ")
    println(model.leastConfidence(words.to))
    model.bestSequence(words.to).asFlatTaggedSequence




    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar evaluation.EvaluationSuite "Stuxnet is a malware"
  }

}