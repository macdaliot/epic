package evaluation

import java.io.File

import scala.io.Source
import epic.sequences.SemiCRF

/**
  * Created by milica on 22/03/16.
  */
  object EpicEvaluation {
  var sentenceConll = ""


    def main(args: Array[String]): Unit = {

      var modelFileName: String = "./data/our_malware.ser.gz"
      if (args.length > 0){
        modelFileName = args(0)
      }
      val modelFile: File = new File(modelFileName)
      val model: SemiCRF[String, String]  = breeze.util.readObject(modelFile)
      val testFileName: String = "./data/epicEvaluationTestFile.txt"
      val testFile: File = new File(testFileName)

      val scores = evaluateModel(testFile, model, false)
      val precision = scores(2)/(scores(2)+scores(0))
      val recall = scores(2)/(scores(2)+scores(1))
      val F1 = 2*precision*recall/(precision+recall)
      println("F1: "+F1+ " Precision: "+precision+" Recall: "+recall)
      println("ScoreArray: " + scores.mkString(" "))

    }

    def evaluateModel(testFile: File, model: SemiCRF[String, String], conll: Boolean): Array[Double]={
      var falsePos = 0.0
      var falseNeg = 0.0
      var truePos = 0.0
      var trueNeg = 0.0
      var nrOfWords = 0.0
      var labelString = ""

      if (conll){
        for (line <- Source.fromFile(testFile).getLines()) {
          if (!line.isEmpty()) {
            sentenceConll += line.split(" ")(0) + " "
            labelString += line.split(" ")(3) + " "
          }
          else {
            val labelSplit = labelString.split(" ")
            val correctLabel = Array.fill(labelSplit.length)(100)
            for (i <- correctLabel.indices){
              if (labelSplit(i) == "O"){
                correctLabel(i) = 2
              }
              else if (labelSplit(i) == "B_MALWARE"){
                correctLabel(i) = 0
              }
              else if (labelSplit(i) == "I_MALWARE"){
                correctLabel(i) = 1
              }


            }
            val words = sentenceConll.split(" ").toSeq
            val epicLabel = model.getBestLabel(words.to)
            nrOfWords += epicLabel.length
            for (i <- epicLabel.indices) {
              if (epicLabel(i) == correctLabel(i)) {
                if (epicLabel(i) < 2) {
                  truePos += 1
                }
                else {
                  trueNeg += 1
                }

              }
              else {
                if (epicLabel(i) < 2) {
                  falsePos += 1
                }
                else {
                  falseNeg += 1
                }
              }
            }
            sentenceConll = ""
            labelString = ""
          }
        }
      }
      else {
        for (line <- Source.fromFile(testFile).getLines()) {
          println("Line: " + line)
          println("Index of: " + line.indexOf("u'conll': u'"))
          var tmpConll: String = line.substring(line.indexOf("u'conll': u'") + 12)
          println("Line: " + tmpConll)
          tmpConll = tmpConll.substring(0, tmpConll.indexOf(", u'"))
          println("Line: " + tmpConll)
          println("Conll: " + tmpConll)
          val correctLabel = getCorrectLabel(tmpConll) //call function
          val words = sentenceConll.split(" ").toSeq
          println("Sentence: " + sentenceConll)
          val epicLabel = model.getBestLabel(words.to)
          println("Correct label: " + correctLabel.mkString(" "))
          println("Epic label: " + epicLabel.mkString(" "))
          nrOfWords += epicLabel.length
          for (i <- epicLabel.indices) {
            if (epicLabel(i) == correctLabel(i)) {
              if (epicLabel(i) < 2) {
                truePos += 1
              }
              else {
                trueNeg += 1
              }

            }
            else {
              if (epicLabel(i) < 2) {
                falsePos += 1
              }
              else {
                falseNeg += 1
              }
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
      sentenceConll = ""
      splitConll = tmp.split("\n")
      val label = Array.fill(splitConll.length)(100)
      for (i <- splitConll.indices) {
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
      label
    }

  }
