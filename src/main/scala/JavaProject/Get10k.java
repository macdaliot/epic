package JavaProject;

import java.nio.file.Files;
import java.util.*;
import java.io.*;
import java.lang.Object;
import java.util.regex.Pattern;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.lang.*;

import epic.sequences.SemiCRF;
import epic.sequences.SemiConllNerPipeline;
import java.lang.Object.*;



public class Get10k {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        File fileNameWordFreq = new File("/Users/" + args[0] + "/Dropbox/Exjobb/PythonThings/wordFreq.txt");
        String s = null;
        // The name of the file to open
        File fileNameUnlabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/nonMalwareSentences.txt");
        String modelFileName = "./data/our_malware_10k.ser.gz";
        System.out.println("Welcome " + args[0]);
        System.out.println("Sit down and let me work my magic");
        int batchSize = 10;

        if (args.length > 1) {
            batchSize = Integer.parseInt(args[1]);
        }

        SelectQueryGet10k sq = new SelectQueryGet10k();
        int modelChoice = 1;
        List<String> batch = new ArrayList<String>();
        batch.add("0.0");
        SemiCRF<String,String> model = getModel.getModel(modelFileName);
        batch = sq.SelectQueryGet10k(fileNameUnlabeledSet, batchSize, modelChoice, model);

        try {
            PrintWriter writer = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/DanielsIds.txt", "UTF-8");
            for (int i = 0; i < batch.size(); i++) {
                writer.println(batch.get(i));
            }

            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException u) {
            System.out.println(u);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");

    }
    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester urName

}