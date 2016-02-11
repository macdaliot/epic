package JavaProject

import java.util.*;
import java.io.*;
import java.lang.Object;
import java.util.regex.Pattern;
import java.nio.file.Paths;
import java.nio.file.Path;

public class Tester {


    public static void main(String[] args) {

        // The name of the file to open.
        File fileNameTrainingSet = new File("/Users/" + args[0] + "/MalwareData/writeTrainingSet.txt");
        File fileNameWordFreq = new File("/Users/" + args[0] + "/Dropbox/Exjobb/PythonThings/wordFreq.txt");
        String modelFileName = "./data/our_malware.ser.gz";

        int batchSize = 5;
        SelectQuery sq = new SelectQuery();
        int modelChoice = 1;
        Batch batch = sq.SelectQuery(fileNameTrainingSet, batchSize, modelChoice, modelFileName);
        List<String> currentSentences = batch.getSentences();
        List<Double> currentIds = batch.getIds();

        String sent1 = "I love pink women";
        String sent2 = "Bunnies are pink";
        CalculateSimilarity cs = new CalculateSimilarity();
        cs.CalculateSimilarity(sent1,sent2, fileNameWordFreq);


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