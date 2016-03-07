package JavaProject;

import epic.sequences.SemiCRF;
import epic.sequences.SemiConllNerPipeline;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;

public class InformationDensity {


    public static void main(String[] args) {
        WordVec allWordsVec = createWordVec(args[0]);
        File fileNameWordFreq = new File("/Users/" + args[0] + "/Dropbox/Exjobb/PythonThings/wordFreq.txt");
        File allSentencesFile = new File("/Users/" + args[0] + "/epic/epic/data/allSentences.txt");
        String s = null;
        double similarities[] = new double[2];
        List<String> allSentences = new ArrayList<>();
        List<Double> ids = new ArrayList<>();
        double simScore = 0;
        double delta = Double.parseDouble(args[1]);
        PrintWriter writer;
        long startTime= System.currentTimeMillis();

        try {
            FileReader tmpR = new FileReader(allSentencesFile);
            BufferedReader tmp = new BufferedReader(tmpR);
            int startIndex;
            while ((s=tmp.readLine()) != null) {
                String[] split = s.split(" ");
                startIndex = split[0].length();
                //System.out.println("Sentence: "+ s.substring(startIndex));
                //System.out.println("ID: "+Double.parseDouble(split[0]));
                allSentences.add(s.substring(startIndex));
                ids.add(Double.parseDouble(split[0]));
            }
            System.out.println("********* FILE WRITING DONE**********");

            writer = new PrintWriter("/Users/" + args[0] + "/epic/epic/data/informationDensity.txt", "UTF-8");
            int c = 0;
            List<double[]> wordVecs1;
            List<double[]> wordVecs2;
            CalculateSimilarity cs = new CalculateSimilarity();
            startTime = System.currentTimeMillis();
            String objectSentence;
            String pairSentence;
            double scores[] = new double[allSentences.size()];
            long startfor;
            long endfor;
            int b = 1;
            int limit =  10;

            for (int obj = 0; obj < limit; obj++) {//allSentences.size()
                objectSentence = allSentences.get(obj).toLowerCase();
                objectSentence = objectSentence.replaceAll("\\p{Punct}+","");
                System.out.println("At sentence "+obj+" with text: \""+objectSentence+"\"");
                wordVecs1 = CreateWordVector(objectSentence,allWordsVec);
                if (wordVecs1.size()==0){
                    System.out.println("***********WordVecs1111********* " + wordVecs1.size());
                    System.out.println("Sentence: "+objectSentence);
                }
                simScore = 0;
                double med = 0;
                startfor = System.currentTimeMillis();
                for (int u = obj; u < allSentences.size();u++) {
                    if((b % 1000)==0) {
                        System.out.println("1000 runs took " + med/1000 + " milliseconds on average");
                        startfor = System.currentTimeMillis();
                    }
                    pairSentence = allSentences.get(u).toLowerCase();
                    pairSentence = pairSentence.replaceAll("\\p{Punct}+","");
                    wordVecs2 = CreateWordVector(pairSentence,allWordsVec);
                    if (wordVecs2.size()==0){
                        System.out.println("***********WordVecs2222********* " + wordVecs2.size());
                        System.out.println("Sentence: "+pairSentence);
                    }
                    similarities = cs.CalculateSimilarity(objectSentence, pairSentence, fileNameWordFreq, allWordsVec,wordVecs1,wordVecs2);
                    scores[u] += similarities[0]*delta+similarities[1]*(1-delta);
                    if(Double.isNaN(scores[u])) {
                        System.out.println("***********NaN-score*********");
                        System.out.println("Object sentence: \"" +objectSentence+"\"");
                        System.out.println("Pair sentence: \""+pairSentence+"\"");
                        System.out.println("Similarities: "+similarities[0]+" "+similarities[1]);
                    }
                    simScore += similarities[0]*delta+similarities[1]*(1-delta);

                    b++;
                    endfor = System.currentTimeMillis();
                    med += endfor-startfor;
                }
                scores[obj] += simScore;
                long currTime = System.currentTimeMillis();
                System.out.println("That took " + (currTime - startTime) + " milliseconds");

                c++;
            }
            for(int u = 0; u < allSentences.size(); u++) {
                System.out.println("Score for object "+ u + " is: " +scores[u]/(2*allSentences.size()));
                System.out.println(u+": "+allSentences.get(u));
                writer.write(Double.toString(scores[u]/(2*allSentences.size()))+"\n");
            }
            writer.close();
            scores = Arrays.copyOfRange(scores, 0,limit-1);
            for (int i = 0; i < limit; i++){
                System.out.println(scores[i]+" \""+ allSentences.get(i) + "\"" );
            }
        } catch (IOException f) {
            System.out.println(f);
        }




            long endTime = System.currentTimeMillis();
            System.out.println("That took " + (endTime - startTime) + " milliseconds");

    }

    public static List<double[]> CreateWordVector(String sent, WordVec allWordsVec){
        List<double[]> wordVecs = new ArrayList<double[]>();
        String[] splitSent = sent.split(" ");
        for(int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);

            //if (wordVector[0]!=-100) {
                wordVecs.add(wordVector);
            //}

        }
        return wordVecs;

    }

    private static WordVec createWordVec(String user){
        System.out.println("******** Create WordVec **********\n");
        File wordVec = new File("/Users/" + user + "/epic/epic/data/wordVecs2D.txt");
        File uniqMals = new File("/Users/" + user + "/epic/epic/data/uniqMals.txt");
        List<String> words = new ArrayList<>();
        List<double[]> vectors = new ArrayList<>();
        WordVec allWords;
        double vector[] = new double[2];
        double n1[] = new double[2];
        double stuxnet[] = new double[2];
        try {
            FileReader tmpW = new FileReader(wordVec);
            BufferedReader tmp = new BufferedReader(tmpW);
            String word;
            String line = null;
            int cMal = 0;
            int cNons = 0;
            boolean number1 = true;
            while ((line=tmp.readLine()) != null) {
                String[] splitLine = line.split(" ");
                word = splitLine[0];
                Arrays.fill(vector, 0.0);
                if (splitLine.length>1) {
                    for (int i = 1; i < splitLine.length; i++) {
                        vector[i - 1] = Double.parseDouble(splitLine[i]);
                    }
                    if (word.equals("stuxnet")){
                        stuxnet = vector.clone();
                    }
                    if (number1){
                        n1 = vector.clone();
                        number1 = false;
                        //System.out.println("1 is set to: "+ Arrays.toString(n1));
                    }
                }
                else{
                    boolean wordIsMalware = false;
                    Scanner scanner = new Scanner(uniqMals);
                    while (scanner.hasNextLine()) {
                        String nextToken = scanner.next();
                        if (nextToken.equalsIgnoreCase(word)) {
                            wordIsMalware = true;
                            break;
                        }
                    }
                    if (wordIsMalware) {
                        cMal++;
                        vector = stuxnet.clone();
                        //System.out.println("Stuxnet "+ Arrays.toString(vector));
                    }
                    else if (NumberUtils.isNumber(word))
                    {
                        //System.out.println("1 "+ Arrays.toString(n1));
                        vector = n1.clone();
                    }
                    else {

                        cNons++;
                        //System.out.println("Whut is "+word);
                        Arrays.fill(vector, -100.0);
                        //System.out.println("Non-foudn vector: "+Arrays.toString(vector));

                    }
                }
                words.add(word);
                vectors.add(vector.clone());
            }
            System.out.println("Number of words: " + words.size());
            System.out.println("Number of unknown mals: "+cMal);
            System.out.println("Number of unknown words (non mal): "+cNons);
            allWords = new WordVec(words,vectors);
        /*for(int i = 0; i< words.size(); i++){
            System.out.println("Word "+ allWords.getWord(i));
            System.out.println("Vector "+ Arrays.toString(allWords.getVector(i)));
        }*/
            return allWords;
        } catch(IOException ex) {
            System.out.println(ex);
        }
        allWords = new WordVec(words,vectors);
        System.out.println("EMPTY ALLWORDS");
        return allWords;
    }

}