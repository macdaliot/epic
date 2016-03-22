package evaluation

import java.io.File

import scala.io.Source
import epic.sequences.SemiCRF

/**
  * Created by milica on 22/03/16.
  */
class EpicEvaluation {
  object EpicEvaluation {


    def main(args: Array[String]): Unit = {

      val modelFileName: String = "./data/our_malware.ser.gz"
      val modelFile: File = new File(modelFileName)
      val model: SemiCRF[String, String]  = breeze.util.readObject(modelFile)
      val testFileName: String = "./data/labeledPool.txt"
      val testFile: File = new File(testFileName)

      val scores = evaluateModel(testFile, model)
      val precision = scores(2)/(scores(2)+scores(0))
      val recall = scores(2)/(scores(2)+scores(1))
      val F1 = 2*precision*recall/(precision+recall)
      println("F1: "+F1+ " Precision: "+precision+" Recall: "+recall)
      println("ScoreArray: " + scores.mkString(" "))

    }

    def evaluateModel(testFile: File, model: SemiCRF[String, String]): Array[Double]={
      var falsePos = 0.0
      var falseNeg = 0.0
      var truePos = 0.0
      var trueNeg = 0.0
      var nrOfWords = 0.0

      for (line <- Source.fromFile(testFile).getLines()) {
        var tmpLine: String = line.substring(line.indexOf("sentence': u") + 13)
        tmpLine = tmpLine.substring(0, tmpLine.indexOf(", u'") - 1)
        tmpLine = tmpLine.replaceAll("\\s+", " ")
        var tmpConll: String = line.substring(line.indexOf("u'conll': u'") + 12)
        tmpConll = tmpConll.substring(0, tmpConll.indexOf(", u'"))
        val words = tmpLine.split(" ").toSeq
        val correctLabel = getCorrectLabel(tmpConll)//call function
        val epicLabel = model.getBestLabel(words.to)
        nrOfWords += epicLabel.length
        for(i <- epicLabel.indices){
          if(epicLabel(i)==correctLabel(i)){
            if(epicLabel(i)<2){
              truePos += 1
            }
            else{
              trueNeg += 1
            }

          }
          else{
            if(epicLabel(i)<2){
              falsePos +=1
            }
            else{
              falseNeg +=1
            }
          }
        }
      }
      val scores = Array(falsePos/nrOfWords,falseNeg/nrOfWords,truePos/nrOfWords,trueNeg/nrOfWords)
      scores
    }

    def getCorrectLabel(conll: String): Array[Int] ={
      var splitConll = conll.split("\\\\n")
      splitConll =  splitConll.slice(0,splitConll.length-2)
      var tmp = ""
      for (i <- splitConll.indices){
        if (splitConll(i)!= " . . "){
          tmp += splitConll(i)+"\n"
        }
      }
      splitConll = tmp.split("\n")
      val label = Array.fill(splitConll.length)(100)
      for (i <- splitConll.indices) {
        val tmp = splitConll(i)
        if(tmp.substring(tmp.length-1)  == " " && tmp.length > 5) {
          label(i) = 2
        }
        else if(tmp.substring(tmp.lastIndexOf(" ")+1) == "B_MALWARE") {
          label(i) = 0
        }

        else if(tmp.substring(tmp.lastIndexOf(" ")+1) == "I_MALWARE") {
          label(i) = 1
        }

      }
      label
    }

  }

}
