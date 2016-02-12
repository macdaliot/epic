package JavaProject;

import java.util.*;
import java.io.*;
import java.lang.Object;
import java.util.regex.Pattern;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.lang.*;

public class Tester {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        // The name of the file to open.
        File fileNameTrainingSet = new File("/Users/" + args[0] + "/MalwareData/writeTrainingSet.txt");
        File fileNameWordFreq = new File("/Users/" + args[0] + "/Dropbox/Exjobb/PythonThings/wordFreq.txt");
        String modelFileName = "./data/our_malware.ser.gz";

        int batchSize = 10;

        if (args.length>1){
        batchSize = Integer.parseInt(args[1]);
        }
        SelectQuery sq = new SelectQuery();
        int modelChoice = 1;
        List<Double> batch = sq.SelectQuery(fileNameTrainingSet, batchSize, modelChoice, modelFileName);

        try {
            Process p = Runtime.getRuntime().exec("python PythonScripts/moveBatch.py");
        }
        catch(IOException ex) {
            System.out.println(
                    "Something went wrong... figure it out");
            }

        String sent1 = "I love pink women";
        String sent2 = "Bunnies are pink";
        //CalculateSimilarity cs = new CalculateSimilarity();
        //cs.CalculateSimilarity(sent1,sent2, fileNameWordFreq);

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");



        /*Simulated querier:
        * Will take the batch of ids and move the unlabeled sentences
        * to a file/database of labeled sentences and removes the current
        * batch from the unlabeled file/database.
        */


        /*Here we should then retrain Epic
        * Train it with a conll file from the labeled database/file.
        */

        /*Finish when there is no more sentences in the unlabeled
        * file/database. (At least for when we're comparing models)
        */
    }

}