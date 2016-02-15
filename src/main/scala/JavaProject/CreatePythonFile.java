package JavaProject;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Created by elin on 12/02/16.
 */
public class CreatePythonFile {

    public void CreatePythonFile(List<Double> list) {

        try {
            PrintWriter writer = new PrintWriter("src/main/scala/JavaProject/PythonScripts/tmp.py", "UTF-8");

            writer.println("import pymongo \nimport sys \nimport os \nfrom pymongo import MongoClient \nfrom moveBatch import moveBatch");
            writer.print("moveBatch([");
                    for (int i = 0; i < list.size()-1; i++) {
                        writer.print(list.get(i)+",");
                    }
            writer.println(list.get(list.size()-1)+"])");
            writer.println("print str(53)" );
            writer.close();
            System.out.println("I did it!");
        }
        catch(IOException ex) {
            System.out.println(
                    "Error: Something went wrong with CreatePythonFile " + ex);
        }

    }
}
