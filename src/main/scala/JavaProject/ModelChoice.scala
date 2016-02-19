package JavaProject

import java.io.File
import epic.sequences.{SemiCRF, TaggedSequence}

object ModelChoice {

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    def getValueModel(model : SemiCRF[String,String], choice: Int, sentence: String):Double = {
        if (choice == 1) {//Least Confidence
            val words = sentence.split(" ").toSeq
            val conf = model.leastConfidence(words.to)
            return -conf
        }
        else {
            return 0;
        }
    }
}
