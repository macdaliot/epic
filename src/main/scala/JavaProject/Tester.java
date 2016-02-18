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
        String s = null;
        // The name of the file to open
        File fileNameUnlabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/unlabeledPool.txt");
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
        SelectQueryRandom sqr = new SelectQueryRandom();
        int modelChoice = 1;
        List<Double> batch = new ArrayList<Double>();
        batch.add(0.0);
        int c = 0;
        try {
            PrintWriter pw = new PrintWriter("data/stats.txt");
            pw.write("Training stats:\n");
            pw.close();
        } catch(FileNotFoundException fe){ System.out.println(fe);}

        if (Integer.parseInt(args[2]) == 1 ) {
            try {
                Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/writeFilesFromDatabase.py 0.8");
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));
                // read the output from the command
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }

                System.out.println("Finished writing from database");
            } catch (IOException ex) {
                System.out.println(
                        "Something went wrong when getRunTime on first training: " + ex);
            }
        }

        String[] trainingString = {"--train",
                "data/labeledPool.conll",
                "--test", "data/conllFileTest.conll",
                "--modelOut", "data/our_malware.ser.gz"};
        SemiConllNerPipeline.main(trainingString);
        System.out.println("Finished training first model");
        /*
        while(true ) {
            c++;
            System.out.println("Batch number " + c + " evaluating");
            batch = sqr.SelectQueryRandom(fileNameUnlabeledSet, batchSize);
            //batch = sq.SelectQuery(fileNameUnlabeledSet, batchSize, modelChoice, modelFileName);
            if(batch.size()==0) {
                break;
            }

            cp.CreatePythonFile(batch);
            try {
                Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/tmp.py");
                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(p.getInputStream()));
                BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(p.getErrorStream()));
                // read the output from the command
                System.out.println("Here is the standard output of the command:\n");
                while ((s = stdInput.readLine()) != null) {
                    System.out.println(s);
                }
            } catch (IOException ex) {
                System.out.println(
                        "Something went wrong when getRunTime of tmp");
            }

            SemiConllNerPipeline.main(trainingString);
        }
*/
        String sent1 = "I love pink women";
        String sent2 = "Bunnies are pink";
        CalculateSimilarity cs = new CalculateSimilarity();
        cs.CalculateSimilarity(sent1,sent2, fileNameWordFreq);

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");
        
    }
    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester urName

}