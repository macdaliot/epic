package JavaProject;

import java.nio.file.Files;
import java.util.*;
import java.io.*;
import java.lang.Object;
import java.util.regex.Pattern;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.lang.*;
import epic.sequences.SemiConllNerPipeline;
import java.lang.Object.*;

public class Tester {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        // The name of the file to open.
        File fileNameTrainingSet = new File("/Users/" + args[0] + "/epic/epic/data/unlabeledPool.txt");
        File fileNameWordFreq = new File("/Users/" + args[0] + "/Dropbox/Exjobb/PythonThings/wordFreq.txt");
        String modelFileName = "./data/our_malware.ser.gz";
        System.out.println("Welcome " + args[0]);
        System.out.println("Sit down and let me work my magic");
        CreatePythonFile cp = new CreatePythonFile();

        int batchSize = 10;

        if (args.length>1){
        batchSize = Integer.parseInt(args[1]);
        }

        SelectQuery sq = new SelectQuery();
        int modelChoice = 1;
        List<Double> batch = new ArrayList<Double>();
        batch.add(0.0);
        int c = 0;
        try {
            PrintWriter pw = new PrintWriter("data/stats.txt");
            pw.write("Training stats:\n");
            pw.close();
        } catch(FileNotFoundException fe){ System.out.println(fe);}


        try {
            Process p = new ProcessBuilder("python","src/main/scala/JavaProject/PythonScripts/writeFilesFromDatabase.py", "0.8").start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String ret = in.readLine();
            System.out.println("value is : "+ret);
            try {
            int wut = p.waitFor();
                System.out.println("Wut is " + wut);
            }
            catch (InterruptedException ex){
                System.out.println("I couldn't wait: " + ex);
            }
            System.out.println("Finished writing from database");
        } catch (IOException ex) {
              System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }

        String[] trainingString = {"--train",
                "data/labeledPool.conll",
                "--test", "data/conllFileTest.conll",
                "--modelOut", "data/our_malware.ser.gz"};
        SemiConllNerPipeline.main(trainingString);
        System.out.println("Finished training first model");

        while(batch.size()>0 ) {
            c++;
            System.out.println("Batch number " + c + " evaluated");
            batch = sq.SelectQuery(fileNameTrainingSet, batchSize, modelChoice, modelFileName);
            cp.CreatePythonFile(batch);
            try {
                Process p = new ProcessBuilder("python", "PythonScripts/tmp.py").start();
                try {
                    int wut = p.waitFor();
                    System.out.println("Wut is " + wut);
                }
                catch (InterruptedException ex){
                    System.out.println("I couldn't wait: " + ex);
                }
            } catch (IOException ex) {
                System.out.println(
                        "Something went wrong when getRunTime of tmp");
            }

            Path tmp = Paths.get("src/main/scala/JavaProject/PythonScripts/tmp.py");

            //try {
            //    Files.deleteIfExists(tmp);
            //} catch (IOException ex) {
            //    System.out.println(
            //            "Trying to delete: " + ex);
            //}
            // Retrain

            SemiConllNerPipeline.main(trainingString);
        }

        //String sent1 = "I love pink women";
        //String sent2 = "Bunnies are pink";
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
    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester urName

}