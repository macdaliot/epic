import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.text.NumberFormat;
public class CreatePythonFile {

    /**
     * Enables simpler communication from java to python by creating a python file which calls moveBatch with the list
     * of ids in list.
     * @param list ids of the sentences in the new batch
     * @param noise level of added simulated noise to the sentences
     * @param newBatch true if a new batch needs to be moved. False if it is supposed to "relabel". Should usually be
     *                 true unless you want to test relabeling for some reason.
     */

    public void CreatePythonFile(List<Double> list, double noise, Boolean newBatch) {

        try {
            PrintWriter writer = new PrintWriter("src/main/scala/JavaProject/PythonScripts/tmp.py", "UTF-8");

            writer.println("#import pymongo \nimport sys \nimport os \n#from pymongo import MongoClient \nfrom moveBatch import moveBatch");
            if (newBatch) { // Move new batch from unlabeled to labeled
                writer.print("rString = moveBatch([");
                for (int i = 0; i < list.size() - 1; i++) {
                    double d = list.get(i);
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(Integer.MAX_VALUE);
                    writer.print(nf.format(d) + ",");
                }
                double d = list.get(list.size() - 1);
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(Integer.MAX_VALUE);
                //System.out.println("Random id format: " +nf.format(d));
                writer.println(nf.format(d) + "]," + noise + ")");
                writer.println("print str(rString)");
                writer.close();
            }
            else { // Add new instance of labeled (chosen for relabeling) to labeled pool
                writer.println("from relabelBatch import relabeledBatch");
                writer.print("rString = relabelBatch([");
                for (int i = 0; i < list.size() - 1; i++) {
                    double d = list.get(i);
                    NumberFormat nf = NumberFormat.getInstance();
                    nf.setMaximumFractionDigits(Integer.MAX_VALUE);
                    writer.print(nf.format(d) + ",");
                }
                double d = list.get(list.size() - 1);
                NumberFormat nf = NumberFormat.getInstance();
                nf.setMaximumFractionDigits(Integer.MAX_VALUE);
                writer.println(nf.format(d) + "]," + noise + ")");
                writer.println("print str(rString)");
                writer.close();
            }
        }
        catch(IOException ex) {
            System.out.println(
                    "Error: Something went wrong with src/main/scala/JavaProject/PythonScripts/tmp.py" + ex);
        }

    }
}
