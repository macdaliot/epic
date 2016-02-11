package JavaProject

import java.io.File
import epic.sequences.{TaggedSequence, SemiCRF}

object ModelChoice {

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    def getValueModel(fileName: String, choice: Int, sentence: String):Double = {
        val modelFile = new File(fileName)
        // instantiate SemiCRF from model file
        val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)
        if (choice == 1) {//Least Confidence
            val words = sentence.split(" ").toSeq
            val conf = model.leastConfidence(words.to)
            return conf
        }
        else {
            return 0;
        }
    }
}
