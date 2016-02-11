import java.io.*;
import java.util.*;
import java.lang.Object;

public class ModelChoice
{
    private final int choice;

    public ModelChoice(int choice, String fileName){
        this.choice = choice;
    }

    /* Set up all calculations, like value of least conf,
    * information density, risk so on.
    * Set up a system to call and combine methods at will.
     */

    public double getValueModel(Object list) {

        if( choice == 1)
        {
            String line = list.toString();
            return line.length();
        }
        else {
            return 0;
        }
    }

}