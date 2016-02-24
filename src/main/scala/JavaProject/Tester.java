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
import epic.sequences.SemiCRF$;
import epic.sequences.SemiConllNerPipeline;
import java.lang.Object.*;

public class Tester {


    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        File fileNameWordFreq = new File("/Users/" + args[0] + "/Dropbox/Exjobb/PythonThings/wordFreq.txt");
        String s = null;
        double noiseParameter = 1;
        // The name of the file to open
        File fileNameUnlabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/unlabeledPool.txt");
        File fileNameLabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/labeledPool.txt");
        String modelFileName = "./data/our_malware.ser.gz";
        System.out.println("Welcome " + args[0]);
        System.out.println("Sit down and let me work my magic");
        CreatePythonFile cp = new CreatePythonFile();
        List<String> sentences;
        Batch b;
        PrintWriter writer;
        double noise = 0.15/(4000/274);
        int totalPoolSize = 0;
        try {
            writer = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/unsure.txt", "UTF-8");

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
            } catch(IOException fe){ System.out.println(fe);}

            String[] trainingString = {"--train",
                    "data/labeledPool.conll",
                    "--test", "data/conllFileTest.conll",
                    "--modelOut", "data/our_malware.ser.gz"};
            if (Integer.parseInt(args[2]) == 1 ) {
                try {
                    Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/writeFilesFromDatabase.py 0.8 1 "+Double.toString(noise));
                    BufferedReader stdInput = new BufferedReader(new
                            InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(p.getErrorStream()));
                    // read the output from the command
                    System.out.println("Here is the standard output of the command writeFiles:\n");
                    while ((s = stdInput.readLine()) != null) {
                        System.out.println(s);
                    }
                } catch (IOException ex) {
                    System.out.println(
                            "Something went wrong when getRunTime on first training: " + ex);
                }
                SemiConllNerPipeline.main(trainingString);
                System.out.println("Finished training first model");

            }
            try{
                FileReader tmpL = new FileReader(fileNameLabeledSet);
            FileReader tmpUn = new FileReader(fileNameUnlabeledSet);
            BufferedReader tmpl = new BufferedReader(tmpL);
            BufferedReader tmpun = new BufferedReader(tmpUn);
            while ((tmpl.readLine()) != null) {
                totalPoolSize++;
            }
            while ((tmpun.readLine()) != null) {
                totalPoolSize++;
            }
            System.out.println("Total pool size is "+ totalPoolSize);
            System.out.println("Finished writing from database");
        } catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }



            boolean labelNewbatch = true;

            while(true ) {
                if (labelNewbatch) {
                    c++;
                    System.out.println("Batch number " + c + " evaluating");
                    SemiCRF<String, String> model = getModel.getModel(modelFileName);
                    //batch = sqr.SelectQueryRandom(fileNameUnlabeledSet, batchSize);


                    if (Integer.parseInt(args[3]) == 1) { //Noise adjustment -> don't pick the hardest
                        double sizeOfLabeledPool = 0.0;
                        try {
                            FileReader tmpR = new FileReader(fileNameLabeledSet);
                            BufferedReader tmp = new BufferedReader(tmpR);
                            while ((tmp.readLine()) != null) {
                                sizeOfLabeledPool++;
                            }
                            System.out.println("Total pool: "+ totalPoolSize+ "  labeled pool: "+sizeOfLabeledPool);
                            int amountToCut = (int) ((sizeOfLabeledPool)*
                                    (sizeOfLabeledPool * noiseParameter/totalPoolSize));
                            System.out.println("Cuttin away "+amountToCut);
                            b = sq.SelectQuery(fileNameUnlabeledSet, batchSize+amountToCut, modelChoice, model);
                            System.out.println("b + cut " + (batchSize+amountToCut));
                            batch = b.getIds();
                            Collections.sort(batch);
                            //System.out.println(Arrays.toString(batch.toArray()));
                            System.out.println("Batch is of length (noise)" + batch.size());
                            if (batch.size()>batchSize) {
                                batch = batch.subList(0, batchSize);
                            }
                            else {System.out.println("Can't cut from batch: batch.size is " + batch.size() + " and batch is " + batchSize);}
                            System.out.println("Batch is of length (noise)" + batch.size());
                        } catch (IOException f) {
                            System.out.println(f);
                        }
                    } else{
                        b = sq.SelectQuery(fileNameUnlabeledSet, batchSize, modelChoice, model);
                        batch = b.getIds();
                        System.out.println("Batch is of length (no noise)" + batch.size());
                    }
                    if (batch.size() == 0) {
                        break;
                    }
                    cp.CreatePythonFile(batch, noise);
                    try {
                        Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/tmp.py");
                        BufferedReader stdInput = new BufferedReader(new
                                InputStreamReader(p.getInputStream()));
                        BufferedReader stdError = new BufferedReader(new
                                InputStreamReader(p.getErrorStream()));
                        // read the output from the command
                        System.out.println("Here is the standard output of the command MoveBatch:\n");
                        while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                        }
                    } catch (IOException ex) {
                        System.out.println(
                                "Something went wrong when getRunTime of tmp");
                    }

                    SemiConllNerPipeline.main(trainingString);
                    if (c == 1000){
                        labelNewbatch = false;
                    }
                }
                else { //"Relabel"
                    System.out.println("************RELABELING********\n******\n ***********");
                    SemiCRF<String, String> model = getModel.getModel(modelFileName);
                    //batch = sqr.SelectQueryRandom(fileNamelabeledSet, batchSize);
                    b = sq.SelectQuery(fileNameLabeledSet, 200, modelChoice, model);
                    sentences = b.getSentences();
                    labelNewbatch = true;
                    batch = b.getIds();
                    int medSentLength = 0;
                    String medLC = "";
                    for (int i = 0; i < batch.size(); i++) {
                        String tmp = sentences.get(i).replace(". . \n","");
                        tmp = tmp.replace(". . B_MALWARE\n","");
                        tmp = tmp.replace(". . I_MALWARE\n","");
                        //System.out.println(tmp);
                        String[] splitSentence = tmp.split(" ");
                        medSentLength += (splitSentence.length-1)/batchSize;
                        medLC += Double.parseDouble(splitSentence[0])/batchSize;
                        writer.println(batch.get(i) + " " +sentences.get(i));
                    }
                    writer.println("Medium LC value: " + medLC+" Medium sent length: " + medSentLength);

                    //if (batch.size() == 0) {
                        break;
                    //}
                    /*cp.CreatePythonFile(batch, noise);
                    try {
                        Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/tmp.py");
                        BufferedReader stdInput = new BufferedReader(new
                                InputStreamReader(p.getInputStream()));
                        BufferedReader stdError = new BufferedReader(new
                                InputStreamReader(p.getErrorStream()));
                        // read the output from the command
                        System.out.println("Here is the standard output of the command MoveBatch:\n");
                        while ((s = stdInput.readLine()) != null) {
                            System.out.println(s);
                        }
                    } catch (IOException ex) {
                        System.out.println(
                                "Something went wrong when getRunTime of tmp");
                    }

                    SemiConllNerPipeline.main(trainingString);*/
                }

            }

            String sent1 = "I have Stuxnet malware in internet";
            String sent2 = "Stuxnet has malware";
            //CalculateSimilarity cs = new CalculateSimilarity();
            //cs.CalculateSimilarity(sent1,sent2, fileNameWordFreq);

            long endTime = System.currentTimeMillis();

            System.out.println("That took " + (endTime - startTime) + " milliseconds");
        } catch (IOException u) {
            System.out.println(u);
        }
    }

    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester urName

}