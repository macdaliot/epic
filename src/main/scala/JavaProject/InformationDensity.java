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
        WordVec allWordsVec = createWordVec(args[0],Integer.parseInt(args[1]));
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
            while ((s = tmp.readLine()) != null) {
                String[] split = s.split(" ");
                startIndex = split[0].length();
                allSentences.add(s.substring(startIndex));
                ids.add(Double.parseDouble(split[0]));
            }
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            allSentencesFile + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + allSentencesFile + "'");
        }
         try {
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
                wordVecs1 = createSentenceWordVector(objectSentence,allWordsVec);
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
                    wordVecs2 = createSentenceWordVector(pairSentence,allWordsVec);
                    similarities = cs.CalculateSimilarity(objectSentence, pairSentence, fileNameWordFreq, allWordsVec,wordVecs1,wordVecs2);
                    scores[u] += similarities[0]*delta+(1-similarities[1])*(1-delta);
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
                writer.write(Double.toString(scores[u]/(2*allSentences.size()))+"\n");
            }
            writer.close();
        } catch (FileNotFoundException ex) {
        System.out.println(
                "Unable to open file '" +
                        "/Users/" + args[0] + "/epic/epic/data/informationDensity.txt" + "'");
    } catch (IOException ex) {
        System.out.println(
                "Error reading file '"
                        + "/Users/" + args[0] + "/epic/epic/data/informationDensity.txt" + "'");
    }




            long endTime = System.currentTimeMillis();
            System.out.println("That took " + (endTime - startTime) + " milliseconds");

    }

    /**
     * Creates word vectors for the current sentence.
     * @param sent The current sentence
     * @param allWordsVec A WordVec object containing vectors for most words found in relevant sentences
     * @return A list of vectors of doubles representing the wordvectors for each word in the sentence. Hence it should
     * the same length as the amount of words in sent.
     */
    public static List<double[]> createSentenceWordVector(String sent, WordVec allWordsVec){
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

    /**
     * Creates a WordVec object containing all the words in the unlabeled pool and their word vectors. Currently
     * needs the name of the user. Should be a relative path.
     * @param user The name of the user.
     * @return A WordVec object
     */

    public static WordVec createWordVec(String user, int dimension){
        System.out.println("******** Create WordVec **********\n");
        File wordVec;
        if (dimension == 2) {
            wordVec = new File("/Users/" + user + "/epic/epic/data/wordVecs2D.txt");
        }
        else {wordVec = new File("/Users/" + user + "/epic/epic/data/wordVecs.txt");}

        File uniqMals = new File("/Users/" + user + "/epic/epic/data/uniqMals.txt");
        List<String> words = new ArrayList<>();
        List<double[]> vectors = new ArrayList<>();
        WordVec allWords;
        double vector[] = new double[dimension];
        double n1[] = new double[dimension];
        double stuxnet[] = new double[dimension];
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
                    }
                    else if (NumberUtils.isNumber(word))
                    {
                        vector = n1.clone();
                    }
                    else {
                        cNons++;
                        Arrays.fill(vector, -100.0);
                    }
                }
                words.add(word);
                vectors.add(vector.clone());
            }
            allWords = new WordVec(words,vectors);
            return allWords;
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            wordVec.getAbsolutePath() + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + wordVec.getAbsolutePath() + "'");
        }
        allWords = new WordVec(words,vectors);
        return allWords;
    }

}