package JavaProject

import epic.sequences.SemiCRF

object MethodChoice {

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    def getValueMethod(models: List[SemiCRF[String, String]], choice: String, sentence: String):Double = {
      val model: SemiCRF[String, String] = models.head
      val words = sentence.split(" ").toSeq
        if (choice.toLowerCase().equals("lc")) {//Least Confidence
            val conf = model.leastConfidence(words.to)
            -conf
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
          - sum
        }
        else if (choice.toLowerCase().equals("vote") ){
          var sum = 0.0
          for (i <- 1 until models.size){
            val posteriors = models(i).getPosteriors(words.to)
            var sumM = 0.0
            for (p <- 0 until posteriors.length){
              sumM += Math.log(posteriors(p))
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
