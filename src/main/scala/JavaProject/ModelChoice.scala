package JavaProject

import java.io.File
import epic.sequences.{SemiCRF, TaggedSequence}

object ModelChoice {

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    def getValueModel(model : SemiCRF[String,String], choice: String, sentence: String):Double = {
      val words = sentence.split(" ").toSeq
        if (choice.toLowerCase().equals("lc")) {//Least Confidence
            val conf = model.leastConfidence(words.to)
            return -conf
        }
        else if (choice.toLowerCase().equals("gibbs") ){
            val posteriors = model.getPosteriors(words.to)
          var sum = 0.0
            for (i <- 0 until posteriors.length)
              {
                sum += posteriors(i)* posteriors(i)
              }
          if(sum>1||sum<0) {
            println("Gibbs sum is: " + sum)
          }
          return 1- sum
        }
        else {
            return 0;
        }
    }
}
