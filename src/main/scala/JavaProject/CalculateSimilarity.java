package JavaProject;

import java.io.*;
import java.util.*;
import java.lang.Object;
import java.lang.*;

public class CalculateSimilarity {
    public double threshold = 0.5;
    public WordVec allWordsVec;

    /**
     * CalculateSimilarity calculates a similarity value between two sentences dependant on word similarity
     * and word order similarity.
     *
     * @param sentWithJunk1 When the sentences (sentenceWithJunk1/2) are added they may
     *                      contain non existent words that will then be removed
     * @param sentWithJunk2 When the sentences (sentenceWithJunk1/2) are added they may
     *                      contain non existent words that will then be removed
     * @param wordFreqs     contains the word frequencies of all words in all the available sentences.
     * @param allWordsVec   contains a vector of all the words' vectors (2D), and wordVecs1/2 contains only the
     *                      word vectors for either sentence
     * @param wordVecs1
     * @param wordVecs2
     * @return
     */

    public double[] CalculateSimilarity(String sentWithJunk1, String sentWithJunk2, List<WordFreq> wordFreqs, WordVec allWordsVec,
                                        List<double[]> wordVecs1, List<double[]> wordVecs2, CosSim cs) {
        this.allWordsVec = allWordsVec;
        double sim[] = {0, 0};

        String uniqueWords = uniqueWordSentence(sentWithJunk1, sentWithJunk2);
        List<double[]> uniqueWordVecs = CreateWordVector(uniqueWords);
        uniqueWords = getFoundWords(uniqueWords);
        String sent1 = getFoundWords(sentWithJunk1);
        String sent2 = getFoundWords(sentWithJunk2);

        if (sent1.length() == 0 | sent2.length() == 0) { // One or both sentences are filled only with nonsensical words
            sim[1] = 1;
            return sim;
        }

        List<double[]> wordSim1 = similarityVectors(wordVecs1, uniqueWordVecs, cs);
        List<double[]> wordSim2 = similarityVectors(wordVecs2, uniqueWordVecs, cs);
        List<double[]> weights1 = WordWeights(wordFreqs, sent1, uniqueWords, wordSim1, sentWithJunk1);
        List<double[]> weights2 = WordWeights(wordFreqs, sent2, uniqueWords, wordSim2, sentWithJunk2);

        double wordSimilarityScore = wordSimilarity(wordSim1.get(0), wordSim2.get(0), weights1, weights2);
        double orderSimilarityScore = orderSimilarity(wordSim1, wordSim2, weights1,
                weights2, sent2, uniqueWords);


        sim[0] = wordSimilarityScore;
        sim[1] = orderSimilarityScore;
        return sim;
    }


    /**
     * Returns a sentence comprised of only existent words in the original sentence sent.
     *
     * @param sent sentence
     * @return sentence without nonsense words
     */

    public String getFoundWords(String sent) {
        String foundUniq = "";
        String[] splitSent = sent.split(" ");
        for (int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);
            if (wordVector[0] != -100) {
                foundUniq += splitSent[i] + " ";
            }
        }
        return foundUniq;

    }

    /**
     * Finds which word in uniqueWordsVec that each word from wordVecs is closest to,
     * also saves the index (friend) in uniqueWords that corresponds to this word
     *
     * @param wordVecs       Word vectors of sentence
     * @param uniqueWordVecs Word vectors of all unique words
     * @param cs             Cosine similarity object for calculations
     * @return Similarity vectors, distance to closest word, alonside that words index
     */
    public List<double[]> similarityVectors(List<double[]> wordVecs, List<double[]> uniqueWordVecs, CosSim cs) {
        double[] shortestDistances = new double[uniqueWordVecs.size()];
        double[] bestFriends = new double[uniqueWordVecs.size()]; //Index of all closest words
        int friend = 0;
        for (int i = 0; i < uniqueWordVecs.size(); i++) // For all unique words
        {
            double currentShortest = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < wordVecs.size(); j++) // Finds closest word in wordVecs
            {
                double tmpDist = cs.CosSim(wordVecs.get(j), uniqueWordVecs.get(i));
                if (tmpDist > currentShortest) {
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
     * Frequencies of words are found in wordFreqs
     *
     * @param wordFreqs of word weights
     * @param sent      sentence
     * @param unique    all unique words in both sentences to be compared
     * @param sim       Values of distances, and closest words to unique words for the sentence
     * @param sentJunk  Sentence with nonsense words included
     * @return Word weights for all words in sentence/unique sentence
     */

    public List<double[]> WordWeights(List<WordFreq> wordFreqs, String sent, String unique, List<double[]> sim, String sentJunk) {
        String[] sentWordsJunk = sentJunk.split(" ");
        String[] sentWords = sent.split(" ");
        String[] uniqueWords = unique.split(" ");
        String friendWord = null;

        double[] weightsSent = new double[uniqueWords.length]; // Weights of closest words in sent to words in uniqueWords
        double[] weightsUnique = new double[uniqueWords.length]; // Weights of words in uniqueWords

        for (int j = 0; j < wordFreqs.size(); j++) { /* For each existing word in the listof words,
                check if it corresponds to the current word, then checks frequency value */
            for (int i = 0; i < uniqueWords.length; i++) {
                if ((wordFreqs.get(j).getWord()).equals(uniqueWords[i])) {
                    weightsUnique[i] = 1 / wordFreqs.get(j).getFreq();
                }
            }
        }

        for (int i = 0; i < uniqueWords.length; i++) {
            int index = Arrays.asList(sentWords).indexOf(uniqueWords[i]);
            if (index >= 0) {
                weightsSent[i] = weightsUnique[i];
            } else {//if(sim.get(0)[i]>=threshold){
                friendWord = sentWordsJunk[(int) sim.get(1)[i]];
                index = Arrays.asList(uniqueWords).indexOf(friendWord);
                weightsSent[i] = weightsUnique[index]; //gets friend in sent
            }
        }


        List<double[]> results = new ArrayList<double[]>();
        results.add(weightsUnique);
        results.add(weightsSent);
        return results;
    }

    /**
     * Calculates word similarity between the sentences, with weighted words.
     *
     * @param vec1     Word vector of sentence 1
     * @param vec2     Word vector of sentence 2
     * @param weights1 weights of sentence 1
     * @param weights2 weights of sentence 2
     * @return Word similarity value
     */

    public double wordSimilarity(double[] vec1, double[] vec2, List<double[]> weights1, List<double[]> weights2) {
        double simSum = 0;
        double vec1Sum = 0;
        double vec2Sum = 0;
        double w1;
        double w2;
        for (int i = 0; i < vec1.length; i++) {
            if (vec1[i] != Double.NEGATIVE_INFINITY && vec2[i] != Double.NEGATIVE_INFINITY) {
                w1 = weights1.get(0)[i] * weights1.get(1)[i];
                w2 = weights2.get(0)[i] * weights2.get(1)[i];
                simSum += vec1[i] * vec2[i] * w1 * w2;
                vec1Sum += vec1[i] * vec1[i] * w1 * w1;
                vec2Sum += vec2[i] * vec2[i] * w2 * w2;
            }
        }

        return simSum / (Math.sqrt(vec1Sum) * Math.sqrt(vec2Sum));
    }

    /**
     * Calculates word order similarity between the sentences, with weighted words
     *
     * @param s1       sentence 1
     * @param s2       sentence 2
     * @param weights1 of sentence 1
     * @param weights2 of sentence 2
     * @return Word order similarity value
     */
    public double orderSimilarity(List<double[]> s1, List<double[]> s2, List<double[]> weights1,
                                  List<double[]> weights2, String sent2, String unique) {
        double[] s1Dist = s1.get(0);
        double[] s1Friend = s1.get(1);
        double[] s2Dist = s2.get(0);
        double[] s2Friend = s2.get(1);
        double[] r1 = new double[s1Dist.length];
        double[] r2 = new double[s2Dist.length];
        String[] sent = sent2.split(" ");
        String[] un = unique.split(" ");
        String word;

        // Specifies word order vectors for either sentence.
        // Threshold specifies that words can be seen as the same if similar enough
        // If same word not found in unique sentence, the order value is 0
        for (int i = 0; i < r1.length; i++) {
            if (s1Dist[i] == 1.0) {
                r1[i] = i + 1;
            } else if (s1Dist[i] >= threshold) {
                r1[i] = s1Friend[i] + 1;

            } else {
                r1[i] = 0;
            }

        }
        for (int i = 0; i < r2.length; i++) {
            if (s2Dist[i] == 1.0) {
                word = un[i];
                r2[i] = Arrays.asList(sent).indexOf(word) + 1;
            } else if (s2Dist[i] >= threshold) {
                r2[i] = s2Friend[i] + 1;

            } else {
                r2[i] = 0.0;
            }

        }
        double numerator = 0.0;
        double denominator = 0.0;
        //Calculate order similarity while avoiding division by 0
        for (int i = 0; i < r1.length; i++) {
            numerator = numerator + Math.pow((r1[i] - r2[i]) * weights1.get(0)[i], 2);
            denominator = denominator + Math.pow((r1[i] + r2[i]) * weights1.get(0)[i], 2);
        }
        numerator = Math.sqrt(numerator);
        denominator = Math.sqrt(denominator);
        if (denominator == 0.0) {
            numerator = 1;
            denominator = 1;
        }

        return numerator / denominator;
    }

    /**
     * Finds all unique words in the two sentences and constructs a "sentence" of these unique words
     *
     * @param s1 sentence 1
     * @param s2 sentence 2
     * @return Unique words in both sentences
     */
    public String uniqueWordSentence(String s1, String s2) {
        String unique = " ";
        String allSent = s1 + " " + s2;
        String[] allWords = allSent.split(" ");
        for (int i = 0; i < allWords.length; i++) {
            if (!unique.contains((" " + allWords[i]) + " ")) {
                unique += allWords[i] + " ";
            }
        }
        return unique;
    }

    /**
     * Finds the word vectors for all words in a sentence.
     *
     * @param sent sentence
     * @return Word vectors for sentence
     */
    public List<double[]> CreateWordVector(String sent) {
        List<double[]> wordVecs = new ArrayList<double[]>();
        String[] splitSent = sent.split(" ");
        for (int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);
            if (wordVector[0] != -100) {
                wordVecs.add(wordVector);
            }
        }
        return wordVecs;

    }

}