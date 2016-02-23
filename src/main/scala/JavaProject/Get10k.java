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
            PrintWriter hash = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/hashIds2.txt", "UTF-8");
            PrintWriter ref = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/refIds2.txt", "UTF-8");
            PrintWriter sent = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/sentences2.txt", "UTF-8");
            System.out.println("Size of batch is  " +batch.size());
            for (int i = 0; i < batch.size(); i++) {
                String line = batch.get(i);
                String[] splitLine = line.split(" ");
                String sentence = "";
                if (splitLine.length>2) {
                    for (int j = 2; j < splitLine.length - 1; j++) {
                        sentence += splitLine[j] + " ";
                    }
                }
                sentence += splitLine[splitLine.length - 1];
                System.out.println("Length of hash "+splitLine[0].length());
                sentence = sentence.replaceAll("\\s+", " ");
                System.out.println("Sentence is: "+sentence);
                if (splitLine[0].length() == 32) {
                    hash.println(splitLine[0]);
                    ref.println(splitLine[1]);
                    String[] sentenceSplit = sentence.split("\"");
                    String sentenceFinal = "=CONCATENATE(";
                    if (sentence.substring(0,1)=="\"")
                        sentenceFinal = sentenceFinal + "CHAR(34),";
                    for (int k = 0; k < sentenceSplit.length-1;k++){
                        sentenceFinal = sentenceFinal + "\"" + sentenceSplit[k] + "\",CHAR(34),";
                    }
                    if (sentence.substring(sentence.length()-2,sentence.length()-1)=="\""){
                        sentenceFinal = sentenceFinal + "\"" + sentenceSplit[sentenceSplit.length-1]  + "\",CHAR(34))";
                    }
                    else {
                        sentenceFinal = sentenceFinal + "\"" + sentenceSplit[sentenceSplit.length-1] + "\")";
                    }
                    sent.println(sentenceFinal);
                }
            }

            hash.close();
            ref.close();
            sent.close();
        } catch (FileNotFoundException | UnsupportedEncodingException u) {
            System.out.println(u);
        }

        long endTime = System.currentTimeMillis();

        System.out.println("That took " + (endTime - startTime) + " milliseconds");

    }
    // java -cp target/scala-2.11/epic-assembly-0.4-SNAPSHOT.jar JavaProject.Tester urName

}