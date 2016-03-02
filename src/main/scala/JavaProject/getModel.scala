package JavaProject

import java.io.File
//import breeze.util.logging
import epic.sequences.SemiCRF



object getModel {

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    def getModel(fileName: String):SemiCRF[String,String] = {
        //val opt = new breeze.optimize.OWLQN {
        //    override val log = breeze.util.logging.NullLogger
        //}
        val modelFile = new File(fileName)
        // instantiate SemiCRF from model file
        val model : SemiCRF[String, String] = breeze.util.readObject(modelFile)
        return model
    }
}
