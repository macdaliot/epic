package JavaProject

import epic.sequences.SemiCRF
import scala.collection.JavaConverters._


object MethodChoice {

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    def getValueMethod(modelsJava: java.util.List[SemiCRF[String, String]], choice: String, sentence: String):Double = {
      val models = modelsJava.asScala.toList
      val model: SemiCRF[String, String] = models.head
      val words = sentence.split(" ").toSeq
        if (choice.toLowerCase().equals("lc")) {//Least Confidence
            val conf = model.leastConfidence(words.to)
            return conf
        }
        else if (choice.toLowerCase().equals("gibbs") ){
          val labels = model.getLabels(words.to)
          val posteriors = model.getPosteriors(words.to,labels)
          var sum = 0.0
            for (i <- 0 until posteriors.length)
              {
                sum += posteriors(i)* posteriors(i)
              }
          if(sum>1||sum<0) {
            println("Gibbs sum is: " + sum)
          }
          - sum
        }
        else if (choice.toLowerCase().equals("vote") ){
          var sum = 0.0
          val labels = models(0).getLabels(words.to)
          for (i <- 1 until models.size){
            val posteriors = models(i).getPosteriors(words.to, labels)
            var sumM = 0.0
            for (p <- 0 until posteriors.length){
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
