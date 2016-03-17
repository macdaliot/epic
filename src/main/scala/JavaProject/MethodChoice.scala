package JavaProject

import epic.sequences.SemiCRF
import scala.collection.JavaConverters._


object MethodChoice {

  /**
    * Uses the chosen active learning method to return scores of the current sentence.
    * @param modelsJava If vote is used this list contains all the voting models. Otherwise this list is only of length
    *                   1 and contains the one big semiCRF model.
    * @param choice A string representing which active learning method is used.
    * @param sentence The sentence to be evaluated.
    * @return A Double representing the score of the current sentence with respect to the model.
    */

    def getValueMethod(modelsJava: java.util.List[SemiCRF[String, String]], choice: String, sentence: String):Double = {
      val models = modelsJava.asScala.toList
      val model: SemiCRF[String, String] = models.head
      val words = sentence.split(" ").toSeq
        if (choice.toLowerCase().equals("lc")) {//Least Confidence
          val conf = model.leastConfidence(words.to)
          -conf
        }
        else if (choice.toLowerCase().equals("gibbs") ){
          val labels = model.getLabels(words.to)
          val posteriors = model.getPosteriors(words.to,labels)
          var sum = 0.0
            for (i <- posteriors.indices)
              {
                sum += posteriors(i)* posteriors(i)
              }
          - sum
        }
        else if (choice.toLowerCase().equals("vote") ){
          var sum = 0.0
          val labels = models.head.getLabels(words.to)
          for (i <- 1 until models.size){
            val posteriors = models(i).getPosteriors(words.to, labels)
            var sumM = 0.0
            for (p <- posteriors.indices){
              if (posteriors(p)>0) {
                sumM += posteriors(p)*Math.log(posteriors(p))
              }
            }
            sum += sumM
          }
          -sum/(models.size-1)
        }
        else {
            0
        }
    }
}
