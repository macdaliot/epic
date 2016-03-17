package JavaProject;

import java.io.*;
import java.util.*;
import java.lang.Object;
import java.lang.*;

public class CalculateSimilarity
{
    public double threshold = 0.2;
    public WordVec allWordsVec;

    /**
     * CalculateSimilarity calculates a similarity value between two sentences dependant on word similarity and word order similarity.
     * @param sentWithJunk1 When the sentences (sentenceWithJunk1/2) are added they may
     *                      contain non existent words that will then be removed
     * @param sentWithJunk2 When the sentences (sentenceWithJunk1/2) are added they may
     *                          contain non existent words that will then be removed
     * @param fileName contains the word frequencies of all words in all the available sentences.
     * @param allWordsVec contains a vector of all the words' vectors (2D), and wordVecs1/2 contains only the
     *                    word vectors for either sentence
     * @param wordVecs1
     * @param wordVecs2
     * @return
     */

    public double[] CalculateSimilarity(String sentWithJunk1,String sentWithJunk2, File fileName, WordVec allWordsVec,
                                        List<double[]> wordVecs1, List<double[]> wordVecs2) {
        this.allWordsVec = allWordsVec;
        CosSim cs = new CosSim();
        double sim[] = {0,0};

        String uniqueWords = uniqueWordSentence(sentWithJunk1, sentWithJunk2);
        List<double[]> uniqueWordVecs = CreateWordVector(uniqueWords);
        uniqueWords = getFoundWords(uniqueWords);
        String sent1 = getFoundWords(sentWithJunk1);
        String sent2 = getFoundWords(sentWithJunk2);

        if(sent1.length()==0 | sent2.length() ==0){ // One or both sentences are filled only with nonsensical words
            return sim;
        }

        List<double[]> wordSim1 = similarityVectors(wordVecs1, uniqueWordVecs, cs);
        List<double[]> wordSim2 = similarityVectors(wordVecs2,uniqueWordVecs, cs);
        List<double[]> weights1 = WordWeights(fileName, sent1, uniqueWords, wordSim1,sentWithJunk1);
        List<double[]> weights2 = WordWeights(fileName, sent2, uniqueWords, wordSim2,sentWithJunk2);

        double wordSimilarityScore = wordSimilarity(wordSim1.get(0),wordSim2.get(0),weights1, weights2);
        double orderSimilarityScore = orderSimilarity(wordSim1, wordSim2, weights1,
                weights2);

        sim[0] = wordSimilarityScore;
        sim[1] = orderSimilarityScore;
        return sim;
    }


    /** Returns a sentence comprised of only existent words in the original sentence sent.
     *
     * @param sent
     * @return
     */

    public String getFoundWords(String sent){
        String foundUniq = "";
        String[] splitSent = sent.split(" ");
        for(int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);
            if (wordVector[0]!=-100) {
                foundUniq += splitSent[i]+" ";
            }
        }
        return foundUniq;

    }

    /**
     * Finds which word in uniqueWordsVec that each word from wordVecs is closest to,
     * also saves the index (friend) in uniqueWords that corresponds to this word
     * @param wordVecs
     * @param uniqueWordVecs
     * @param cs
     * @return
     */
    public List<double[]> similarityVectors(List<double[]> wordVecs,List<double[]> uniqueWordVecs, CosSim cs){
        double[] shortestDistances = new double[uniqueWordVecs.size()];
        double[] bestFriends = new double[uniqueWordVecs.size()]; //Index of all closest words
        int friend = 0;
        for(int i = 0; i < uniqueWordVecs.size();i++) // For all unique words
        {
            double currentShortest = Double.NEGATIVE_INFINITY;
            for(int j = 0; j <wordVecs.size();j++) // Finds closest word in wordVecs
            {
                double tmpDist = cs.CosSim(wordVecs.get(j), uniqueWordVecs.get(i));
                if(tmpDist> currentShortest)
                {
                    currentShortest = tmpDist;
                    friend = j;

                }
            }
            shortestDistances[i] = currentShortest;
            bestFriends[i] = friend;

        }




        List<double[]> results = new ArrayList<>();
        results.add(shortestDistances);
        results.add(bestFriends);
        return results;
    }

    /**
     * Finds the weights of a word, both concerning the weight of the word itself, but also its closest friend in
     * the unique words. Note that if the word in the sentence exists in unique words these will be the same
     * The weights are inversely proportional to the frequency of the word
     * Frequencies of words are found in fileName
     * @param fileName
     * @param sent
     * @param unique
     * @param sim
     * @param sentJunk
     * @return
     */

    public List<double[]> WordWeights(File fileName, String sent, String unique, List<double[]> sim, String sentJunk ){
        String[] sentWordsJunk = sentJunk.split(" ");
        String[] sentWords = sent.split(" ");
        String[] uniqueWords = unique.split(" ");
        String friendWord = null;

        double[] weightsSent = new double[uniqueWords.length]; // Weights of closest words in sent to words in uniqueWords
        double[] weightsUnique = new double[uniqueWords.length]; // Weights of words in uniqueWords

        try { // To catch if fileName doesn't open
            String line = null;
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) { /* For each existing word in the file fileName,
                check if it corresponds to the current word, then checks frequency value */
                line = " " + line;
                for(int i = 0; i < uniqueWords.length; i++) {
                    if(line.contains(" " + uniqueWords[i]+" ")) {
                        String tmp = line.substring(line.indexOf(uniqueWords[i]) + uniqueWords[i].length() + 1);
                        weightsUnique[i]= 1/Double.parseDouble(tmp);
                        int index = Arrays.asList(sentWords).indexOf(uniqueWords[i]);
                        if (index>=0) {
                            weightsSent[i] = 1 / Double.parseDouble(tmp);
                        }
                        else if(sim.get(0)[i]>threshold){
                            try {
                                friendWord = sentWordsJunk[(int) sim.get(1)[i]];
                                index = Arrays.asList(uniqueWords).indexOf(friendWord);
                                weightsSent[index] = 1 / Double.parseDouble(tmp); //gets friend in sent
                            }
                            catch(ArrayIndexOutOfBoundsException a){
                                System.out.println(a);
                                System.exit(1);
                            }
                        }
                    }
                }


            }
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
        }
        List<double[]> results = new ArrayList<double[]>();
        results.add(weightsUnique);
        results.add(weightsSent);
        return results;
    }

    /**
     * Calculates word similarity between the sentences, with weighted words.
     * @param vec1
     * @param vec2
     * @param weights1
     * @param weights2
     * @return
     */

    public double wordSimilarity(double[] vec1, double[] vec2, List<double[]> weights1, List<double[]> weights2) {
        double simSum = 0;
        double vec1Sum = 0;
        double vec2Sum = 0;
        double w1;
        double w2;
        for(int i = 0; i < vec1.length; i++){
            if(vec1[i]!=Double.NEGATIVE_INFINITY && vec2[i]!=Double.NEGATIVE_INFINITY) {
                w1 = weights1.get(0)[i] * weights1.get(1)[i];
                w2 = weights2.get(0)[i] * weights2.get(1)[i];
                simSum += vec1[i] * vec2[i] * w1 * w2;
                vec1Sum += vec1[i] * vec1[i] * w1 * w1;
                vec2Sum += vec2[i] * vec2[i] * w2 * w2;
            }
        }

        return simSum/(Math.sqrt(vec1Sum)*Math.sqrt(vec2Sum));
    }

    /**
     * Calculates word order similarity between the sentences, with weighted words
     * @param s1
     * @param s2
     * @param weights1
     * @param weights2
     * @return
     */
    public double orderSimilarity(List<double[]> s1, List<double[]> s2, List<double[]> weights1,
                                  List<double[]> weights2) {
        double[] s1Dist = s1.get(0);
        double[] s1Friend = s1.get(1);
        double[] s2Dist = s2.get(0);
        double[] s2Friend = s2.get(1);
        double[] r1 = new double[s1Dist.length];
        double[] r2 = new double[s2Dist.length];

        // Specifies word order vectors for either sentence.
        // Threshold specifies that words can be seen as the same if similar enough
        // If same word not found in unique sentence, the order value is 0
        for(int i =0; i< r1.length;i++){
            if(s1Dist[i]==1.0){
                r1[i]=i+1;
            }else if(s1Dist[i]>=threshold) {
                r1[i] = s1Friend[i]+1;

            }else{
                r1[i]=0;
            }

        }
        for(int i =0; i< r2.length;i++){
            if(s2Dist[i]==1.0){
                r2[i]=i+1;
            }else if(s2Dist[i]>=threshold) {
                r2[i] = s2Friend[i]+1;

            }else{
                r2[i]=0.0;
            }

        }

        double numerator = 0.0;
        double denominator = 1.0;
        //Calculate order similarity while avoiding division by 0
        for(int i = 0; i< r1.length; i ++){
            if (r1[i]==0.0| r2[i]==0.0) {
                numerator += Math.pow(r1.length * weights1.get(1)[i] * weights2.get(1)[i], 2);
                if (numerator!=0) {
                    denominator += numerator;
                }
                else { denominator = 1;}}
            else {
                numerator = numerator + Math.pow((r1[i] - r2[i]) * weights1.get(1)[i] * weights2.get(1)[i], 2);
                denominator = denominator + Math.pow((r1[i] + r2[i]) * weights1.get(1)[i] * weights2.get(1)[i], 2);
            }
        }
        numerator = Math.sqrt(numerator);
        denominator = Math.sqrt(denominator);

        return numerator/denominator;
    }

    /**
     * Finds all unique words in the two sentences and constructs a "sentence" of these unique words
     * @param s1
     * @param s2
     * @return
     */
    public String uniqueWordSentence(String s1, String s2)
    {
        String unique = " ";
        String allSent = s1 + " " + s2;
        String[] allWords = allSent.split(" ");
        for (int i = 0; i < allWords.length;i++){
            if( !unique.contains((" "+allWords[i])+" ")){
                unique += allWords[i]+" ";
            }
        }
        return unique;
    }

    /**
     * Finds the word vectors for all words in a sentence.
     * @param sent
     * @return
     */
    public List<double[]> CreateWordVector(String sent){
        List<double[]> wordVecs = new ArrayList<double[]>();
        String[] splitSent = sent.split(" ");
        for(int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);
            if (wordVector[0]!=-100) {
                wordVecs.add(wordVector);
            }
        }
        return wordVecs;

    }

}