package JavaProject

import java.io.File
//import breeze.util.logging
import epic.sequences.SemiCRF



object getModel {

  /**
    * Opens up the trained semiCRFs
    * @param fileName The file name of the semiCRF to be opened
    * @return the model
    */

    def getModel(fileName: String):SemiCRF[String,String] = {
        val modelFile = new File(fileName)
        val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)
        model
    }
}
