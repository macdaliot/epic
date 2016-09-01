package evaluation
import java.io.File
import epic.sequences.{Segmentation, SemiCRF}

object Demo {

  def main(args: Array[String]) : Unit = {
    var ok = true
    if (args(0)==null) {
      println("Please provide a model path")
      ok = false
    } else if (args(1)==null) {
      println("Please provide text to analyze")
      ok = false
    }
    if (ok) {
      classify(args(0), args(1))
    }
  }

  def classify(modelPath : String, s : String) : Unit = {
    val modelFile = new File(modelPath)
    println("Running with model "+modelFile)

    // instantiate SemiCRF from model file
    val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)

    // preprocess sentence
    val words = s.split(" ").toSeq

    val bestSequenceExtended: ((Double, Seq[Double]), Segmentation[String, String]) = model.bestSequenceExtended(words.to)
    val segmentation: Segmentation[String, String] = bestSequenceExtended._2
    val tuples: IndexedSeq[(String, Double)] = segmentation.words.zip(bestSequenceExtended._1._2)

    println("Render:      "+segmentation.render)
    println("Score:       "+tuples)
    println("Total score: "+bestSequenceExtended._1._1)
  }
}