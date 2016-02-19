package JavaProject

import java.io.File

import epic.sequences.SemiCRF

object getModel {

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    def getModel(fileName: String):SemiCRF[String,String] = {
        val modelFile = new File(fileName)
        // instantiate SemiCRF from model file
        val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)
        return model
    }
}
