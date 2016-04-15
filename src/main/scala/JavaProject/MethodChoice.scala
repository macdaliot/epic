package JavaProject

import epic.sequences.SemiCRF
import scala.collection.JavaConverters._


object MethodChoice {

  /**
    * Uses the chosen active learning method to return scores of the current sentence.
    *
    * @param modelsJava If vote is used this list contains all the voting models. Otherwise this list is only of length
    *                   1 and contains the one big semiCRF model.
    * @param choice A string representing which active learning method is used.
    * @param sentence The sentence to be evaluated.
    * @return A Double representing the score of the current sentence with respect to the model.
    */

    def getValueMethod(modelsJava: java.util.List[SemiCRF[String, String]], choice: String, sentence: String, conll: String, score: Double):Double = {
      val models = modelsJava.asScala.toList
      val model: SemiCRF[String, String] = models.head
      var words = sentence.split(" ").toSeq
    if (words.head == ""){
      words = words.slice(1,words.size)
    }

        if (choice.toLowerCase().contains("lc")) {//Least Confidence
          var conf = model.leastConfidence(words.to)
          if (score != -1){
            conf = conf*(1-score)
          }
          -conf
        }
        else if (choice.toLowerCase().contains("gibbs") ){
          val labels = model.getLabels(words.to)
          val posteriors = model.getPosteriors(words.to,labels)
          var sum = 0.0
            for (i <- posteriors.indices)
              {
                sum += posteriors(i)* posteriors(i)
              }
          if (score != -1){
            sum = (1-score)*sum
          }
          -sum
        }
        else if (choice.toLowerCase().contains("vote") ){
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
          if (score != -1){
            sum = score*sum
          }
          -sum/(models.size-1)
        }
        else if (choice.toLowerCase().equals("error") ){
          var splitConll = conll.split("\\\\n")
          splitConll =  splitConll.slice(0,splitConll.length-2)
          var tmp = ""
          for (i <- 0 until splitConll.length){
            if (splitConll(i)!= " . . "){
              tmp += splitConll(i)+"\n"
            }
          }
          splitConll = tmp.split("\n")
          val label = Array.fill(splitConll.length)(100)
          var sentenceConll = ""
          for (i <- 0 until splitConll.length) {
            val tmp = splitConll(i)
            if(tmp.substring(tmp.length-1)  == " " && tmp.length > 5) {
              label(i) = 2
              sentenceConll += tmp.replace(" . . ","")+" "
            }
            else if(tmp.substring(tmp.lastIndexOf(" ")+1) == "B_MALWARE") {
              label(i) = 0
              sentenceConll += tmp.replace(" . . B_MALWARE","")+" "
            }

            else if(tmp.substring(tmp.lastIndexOf(" ")+1) == "I_MALWARE") {
              label(i) = 1
              sentenceConll += tmp.replace(" . . I_MALWARE","")+" "
            }

          }
          val wordsConll = sentenceConll.split(" ").toSeq
          -model.getScoreOfLabel(wordsConll.to, label)
        }
        else if (score != -1) {
          score
        }
        else {
            0
        }
    }
}
