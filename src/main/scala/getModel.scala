import java.io.File

import epic.sequences.SemiCRF

/**
  * Created by langkilde on 9/2/16.
  */
object getModel {

  def getModel(fileName: String) : SemiCRF[String, String] = {
    val modelFile = new File(fileName)
    val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)
    model
  }

}
