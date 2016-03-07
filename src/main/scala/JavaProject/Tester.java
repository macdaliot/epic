package JavaProject;

import java.nio.file.Files;
import java.util.*;
import java.io.*;
import java.lang.Object;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.regex.Pattern;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.lang.*;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

import epic.sequences.SemiCRF;
import epic.sequences.SemiCRF$;
import epic.sequences.SemiConllNerPipeline;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.Object.*;

public class Tester {
    public static int batchSize = 10;
    public static String methodChoice = "LC";
    public static double noise = 0;//0.15/(4000/274);
    public static boolean boo =true;
    public static String[] trainingString ={"--train",
            "data/labeledPool.conll",
            "--test", "data/conllFileTest.conll",
            "--modelOut", "data/our_malware.ser.gz","--useStochastic","false","--regularization","1"};
    public static boolean noiseCut = false;
    public static boolean error = false;

    public static void main(String[] args) {
        // First input is user name
        // second input is batch size
        // Input "train" to start the run of with training
        // Input "stochastic" to use the stochastic epic model
        // Input gibbs/lc to choose active learning method
        // Input "noise" to add batch cutting
        // Input "error" for error adjustment
        // Input "db" to retrain the database
        setStaticVariables(args);

        System.out.println("Welcome " + args[0]);
        long startTime = System.currentTimeMillis();
        double noiseParameter = 1;
        String s = null;

        copyFile(args[0]); //Copys sets to txt files

        // Filenames
        File fileNameUnlabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/unlabeledPool.txt");
        File fileNameLabeledSet = new File("/Users/" + args[0] + "/epic/epic/data/labeledPool.txt");
        String modelFileName = "./data/our_malware.ser.gz";

        // Initialize objects
        SelectQuery sq = new SelectQuery();
        SelectQueryRandom sqr = new SelectQueryRandom();
        CreatePythonFile cp = new CreatePythonFile();
        Batch b;
        PrintWriter writer;
        int totalPoolSize = 0;

        try {
            writer = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/unsure.txt", "UTF-8");

            List<Double> batch = new ArrayList<Double>();
            batch.add(0.0);
            int c = 0;
            try {
                PrintWriter pw = new PrintWriter("data/stats.txt");
                pw.write("Training stats:\n");
                pw.close();
            } catch(IOException fe){ System.out.println(fe);}

            totalPoolSize = getPoolSize(fileNameLabeledSet, fileNameUnlabeledSet);
            boolean labelNewBatch = true;

            while(boo ) {
                if (labelNewBatch) {
                    c++;
                    System.out.println("******** Batch number " + c + " evaluating **********\n");
                    SemiCRF<String, String> model = getModel.getModel(modelFileName);
                    //batch = sqr.SelectQueryRandom(fileNameUnlabeledSet, batchSize);
                    if (noiseCut) { //Noise adjustment -> don't pick the hardest
                        double sizeOfLabeledPool = 0.0;
                        try {
                            FileReader tmpR = new FileReader(fileNameLabeledSet);
                            BufferedReader tmp = new BufferedReader(tmpR);
                            while ((tmp.readLine()) != null) {
                                sizeOfLabeledPool++;
                            }
                            double sizeOfUnlabeledPool = 0.0;
                            FileReader tmpU = new FileReader(fileNameUnlabeledSet);
                            tmp = new BufferedReader(tmpU);
                            while ((tmp.readLine()) != null) {
                                sizeOfUnlabeledPool++;
                            }
                            System.out.println("Size unlabeled: "+ sizeOfUnlabeledPool);
                            System.out.println("Total pool: "+ totalPoolSize+ "  labeled pool: "+sizeOfLabeledPool);
                            int amountToCut = (int) ((sizeOfLabeledPool)*
                                    (sizeOfLabeledPool * noiseParameter/totalPoolSize));
                            System.out.println("Cuttin away "+amountToCut);
                            b = sq.SelectQuery(fileNameUnlabeledSet, batchSize+amountToCut, methodChoice, model);
                            System.out.println("b + cut " + (batchSize+amountToCut));
                            batch = b.sortBatch();
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
                        b = sq.SelectQuery(fileNameUnlabeledSet, batchSize, methodChoice, model);
                        batch = b.getIds();
                        System.out.println("Batch is of length (no noise)" + batch.size());
                    }
                    if (batch.size() == 0) {
                        break;
                    }
                    moveBatch(cp,noise,batch,labelNewBatch);
                    SemiConllNerPipeline.main(trainingString);
                    if (c == 1000){
                        labelNewBatch = false;
                    }
                }
                else if (!labelNewBatch & error) { //"Relabel"
                    System.out.println("************RELABELING********\n" +
                            "              ******\n             **********\n");
                    SemiCRF<String, String> model = getModel.getModel(modelFileName);
                    //batch = sqr.SelectQueryRandom(fileNamelabeledSet, batchSize);
                    b = sq.SelectQuery(fileNameLabeledSet, 200, methodChoice, model);
                    writeUnsure(b,batchSize, writer);

                    if (batch.size() == 0) {
                    break;
                    }

                    moveBatch(cp,noise,batch,labelNewBatch);

                    SemiConllNerPipeline.main(trainingString);

                    labelNewBatch = true;
                }

            }


            /*String sent1 = "I have Stuxnet malware in my internet";
            String sent2 = "Stuxnet has malware";
            CalculateSimilarity cs = new CalculateSimilarity();
            cs.CalculateSimilarity(sent1,sent2, fileNameWordFreq, allWordVecs);*/

            long endTime = System.currentTimeMillis();

            System.out.println("That took " + (endTime - startTime) + " milliseconds");
        } catch (IOException u) {
            System.out.println(u);
        }
    }

    private static void copyFile(String user){
        File sourceFile1 = new File("/Users/" + user + "/epic/epic/data/unlabeledPoolStart.txt");
        File destFile1 = new File("/Users/" + user + "/epic/epic/data/unlabeledPool.txt");
        File sourceFile2 = new File("/Users/" + user + "/epic/epic/data/labeledPoolStart.txt");
        File destFile2 = new File("/Users/" + user + "/epic/epic/data/labeledPool.txt");
        File sourceFile3 = new File("/Users/" + user + "/epic/epic/data/labeledPoolStart.conll");
        File destFile3 = new File("/Users/" + user + "/epic/epic/data/labeledPool.conll");

        FileChannel source1 = null;
        FileChannel destination1 = null;
        FileChannel source2 = null;
        FileChannel destination2 = null;
        FileChannel source3 = null;
        FileChannel destination3 = null;
        try{
            try {
                source1 = new FileInputStream(sourceFile1).getChannel();
                destination1 = new FileOutputStream(destFile1).getChannel();
                destination1.transferFrom(source1, 0, source1.size());
                source2 = new FileInputStream(sourceFile2).getChannel();
                destination2 = new FileOutputStream(destFile2).getChannel();
                destination2.transferFrom(source2, 0, source2.size());
                source3 = new FileInputStream(sourceFile3).getChannel();
                destination3 = new FileOutputStream(destFile3).getChannel();
                destination3.transferFrom(source3, 0, source3.size());
            }
            finally {
                if(source1 != null) {
                    source1.close();
                }
                if(destination1 != null) {
                    destination1.close();
                }
                if(source2 != null) {
                    source2.close();
                }
                if(destination2 != null) {
                    destination2.close();
                }
                if(source3 != null) {
                    source3.close();
                }
                if(destination3 != null) {
                    destination3.close();
                }
            }
        }
        catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }


    }
    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester urName


    private static void splitAndWriteDB(double noise){
        System.out.println("******** Create all pools and datasets **********\n");
        String s = null;
        try {
            Process p = Runtime.getRuntime().exec("python src/main/scala/JavaProject/PythonScripts/writeFilesFromDatabase.py 0.8 1"+Double.toString(noise));
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

    }

    private static int getPoolSize(File fileNameLabeledSet, File fileNameUnlabeledSet){
        int size = 0;
        try{
            FileReader tmpL = new FileReader(fileNameLabeledSet);
            FileReader tmpUn = new FileReader(fileNameUnlabeledSet);
            BufferedReader tmpl = new BufferedReader(tmpL);
            BufferedReader tmpun = new BufferedReader(tmpUn);
            while ((tmpl.readLine()) != null) {
                size++;
            }
            while ((tmpun.readLine()) != null) {
                size++;
            }
            System.out.println("Total pool size is "+ size);
        } catch (IOException ex) {
            System.out.println(
                    "Something went wrong when getRunTime on first training: " + ex);
        }
        return size;

    }

    private static void moveBatch(CreatePythonFile cp, double noise,List<Double> batch, Boolean newBatch){
        System.out.println("******** Move a batch **********");
        cp.CreatePythonFile(batch, noise, newBatch);
        String s=null;
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

    }

    private static void writeUnsure(Batch b, int batchSize,PrintWriter writer){

        List<Double>batch = b.getIds();
        List<String>sentences = b.getSentences();
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
    }

    private static void setStaticVariables(String[] args){
        System.out.println("*********************************************");
        System.out.println("****************Set Variables****************");
        for (int i = 0; i<args.length; i++) {
            args[i] = args[i].toLowerCase();
        }
        if (args.length>1){
            batchSize = Integer.parseInt(args[1]);
            System.out.println("batchSize has been manually set to: "+batchSize);
        }

        if (Arrays.asList(args).contains("gibbs")) {
            methodChoice = "gibbs";
            System.out.println("Method has been manually set to Gibbs");
        }
        else{
            System.out.println("Method has been set to default LC");
        }


        if (Arrays.asList(args).contains("stochastic")){
            trainingString[7]= "true";
            System.out.println("Epic uses stochastic training");
        }
        else{
            System.out.println("Epic uses non-stochastic training");
        }

        if(Arrays.asList(args).contains("train")){
            SemiConllNerPipeline.main(trainingString);
            System.out.println("Run starts with training the model before batch selection");
        }

        if(Arrays.asList(args).contains("error")){
            error = true;
            System.out.println("Error adjustment active. Relabeling included.");
        }

        if(Arrays.asList(args).contains("noise")){
            noiseCut = true;
            System.out.println("Noise reduction active. Batch cutting included");
        }
        if (Arrays.asList(args).contains("db")) {
            splitAndWriteDB(noise);
            boo = false;
            System.out.println("Database is rewritten");

        }

        System.out.println("****************Variables Set****************");
        System.out.println("*********************************************");



    }
}