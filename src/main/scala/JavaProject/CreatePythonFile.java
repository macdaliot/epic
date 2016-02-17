package JavaProject;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.text.NumberFormat;

/**
 * Created by elin on 12/02/16.
 */
public class CreatePythonFile {

    public void CreatePythonFile(List<Double> list) {

        try {
            PrintWriter writer = new PrintWriter("src/main/scala/JavaProject/PythonScripts/tmp.py", "UTF-8");

            writer.println("import pymongo \nimport sys \nimport os \nfrom pymongo import MongoClient \nfrom moveBatch import moveBatch");
            writer.print("rString = moveBatch([");
            System.out.println("Random id format: "+list.size());
                    for (int i = 0; i < list.size()-1; i++) {
                        double d = list.get(i);
                        NumberFormat nf = NumberFormat.getInstance();
                        nf.setMaximumFractionDigits(Integer.MAX_VALUE);
                        System.out.println("Random id format: " +nf.format(d));
                        writer.print(nf.format(d)+",");
                    }
            double d = list.get(list.size()-1);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(Integer.MAX_VALUE);
            System.out.println("Random id format: " +nf.format(d));
            writer.println(nf.format(d)+"])");
            writer.println("print str(rString)" );
            writer.close();
            System.out.println("I did it!");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error: Something went wrong with CreatePythonFile " + ex);
        }

    }
}
