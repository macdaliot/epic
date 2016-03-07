package JavaProject;

import java.io.*;
import java.util.*;
import java.lang.Object;
import java.lang.*;

public class CalculateSimilarity
{
    public double threshold = 0.2;
    public WordVec allWordsVec;

    public double[] CalculateSimilarity(String sentWithJunk1,String sentWithJunk2, File fileName, WordVec allWordsVec,
                                        List<double[]> wordVecs1, List<double[]> wordVecs2) {
        this.allWordsVec = allWordsVec;
        for(int i = 0; i< wordVecs1.size(); i++){
            if(wordVecs1.get(i).length>2) {
                System.out.println("Too long vector for sentence \"" + sentWithJunk1 + "\" for index " + i);
                System.out.println(i + " : " +Arrays.toString(wordVecs1.get(i)));
            }
        }
        for(int i = 0; i< wordVecs2.size(); i++){
            if(wordVecs2.get(i).length>2) {
                System.out.println("Too long vector for sentence \"" + sentWithJunk2 + "\" for index " + i);
                System.out.println(i+ " : " +Arrays.toString(wordVecs2.get(i)));
            }
        }
        CosSim cs = new CosSim();
        String uniqueWords = uniqueWordSentence(sentWithJunk1, sentWithJunk2);
        //System.out.println("**************Unique before ifFound: " +uniqueWords+"****************");
        List<double[]> uniqueWordVecs = CreateWordVector(uniqueWords);
        uniqueWords = getFoundWords(uniqueWords);
        //System.out.println("**************Unique after ifFound: " +uniqueWords+"****************");
        String sent1 = getFoundWords(sentWithJunk1);
        String sent2 = getFoundWords(sentWithJunk2);
        double sim[] = {0,0};
        if(sent1.length()==0 | sent2.length() ==0){
            if(sent1.length()==0)
                System.out.println("Sentence is all nonsense: \""+sentWithJunk1+"\"");
            else
                System.out.println("Sentence is all nonsense: \""+sentWithJunk2+"\"");
            return sim;
        }
        List<double[]> wordSim1 = similarityVectors(wordVecs1, uniqueWordVecs, cs,uniqueWords, sent1);
        List<double[]> wordSim2 = similarityVectors(wordVecs2,uniqueWordVecs, cs,uniqueWords,sent2);
        List<double[]> weights1 = WordWeights(fileName, sent1, uniqueWords, wordSim1,sentWithJunk1);
        List<double[]> weights2 = WordWeights(fileName, sent2, uniqueWords, wordSim2,sentWithJunk2);
        double wordSimilarityScore = wordSimilarity(wordSim1.get(0),wordSim2.get(0),weights1, weights2);
        double orderSimilarityScore = orderSimilarity(wordSim1, wordSim2, weights1,
                weights2);
        sim[0] = wordSimilarityScore;
        sim[1] = orderSimilarityScore;
        //System.out.println("Word sim: " +wordSimilarityScore + " Order sim: "+orderSimilarityScore);


        /*for(int i = 0; i< 6; i++){
            System.out.println("Word "+ allWordsVec.getWord(i));
            System.out.println("Vector "+ Arrays.toString(allWordsVec.getVector(i)));
        }*/
        return sim;
    }

    // Creates a list of vectors where each instance in the list corresponds to a word in the sentence sent,
    // and each vector the vector representation of that word.


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


    // Finds which word in all the uniqueWords that a word from the sentence (wordVecs) is closest to,
    // also saves the index in uniqueWords that corresponds to this word.
    public List<double[]> similarityVectors(List<double[]> wordVecs,List<double[]> uniqueWordVecs, CosSim cs,String uniqueSent, String sent){
        double[] shortestDistances = new double[uniqueWordVecs.size()];
        double[] bestFriends = new double[uniqueWordVecs.size()]; //Index of closest word
        int friend = 0;
        String[] splitSent = sent.split(" ");
        String[] split = uniqueSent.split(" ");
        boolean isEverIn = false;
        for(int i = 0; i < uniqueWordVecs.size();i++) // For all unique words
        {
            boolean isIn = false;
            double currentShortest = Double.NEGATIVE_INFINITY;
            for(int j = 0; j <wordVecs.size();j++) // Finds closest word in wordVecs
            {
                //System.out.println("Wordvecs size: "+wordVecs.size()+" :Now at "+j);
                //System.out.println("SplitSent: "+Arrays.toString(splitSent)+" : "+splitSent.length);
                //System.out.println("UniqueSplitSent: "+Arrays.toString(split)+" : "+split.length);
                //System.out.println("UniqueWordvecs size: "+uniqueWordVecs.size()+" :Now at "+i);
                //System.out.println("Word1: "+splitSent[j]+ " Word2: " + split[i]);
                double tmpDist = cs.CosSim(wordVecs.get(j), uniqueWordVecs.get(i));
                if(tmpDist> currentShortest)
                {
                    isIn = true;
                    currentShortest = tmpDist;
                    friend = j;
                    isEverIn = true;

                }
            }
            shortestDistances[i] = currentShortest;
            bestFriends[i] = friend;

        }
        if(!isEverIn){
            System.out.println("!!!!!!!!!Never in!!!!!!!!!\n");
        }



        List<double[]> results = new ArrayList<double[]>();
        results.add(shortestDistances);
        results.add(bestFriends);
        return results;
    }


    // Finds the weights of a word, both concerning the weight of the word itself, and its closest friend in
    // the unique words. The weights are inversly proportional to the frequency of the word
    // Frequences of words are found in fileName
    public List<double[]> WordWeights(File fileName, String sent, String unique, List<double[]> sim, String sentJunk ){
        String[] sentWordsJunk = sentJunk.split(" ");
        String[] sentWords = sent.split(" ");
        String[] uniqueWords = unique.split(" ");
        //System.out.println("******* WordWeights ********");
        String friendWord = null;

        double[] weightsSent = new double[uniqueWords.length]; // Weights of closest words in sent to words in uniqueWords
        double[] weightsUnique = new double[uniqueWords.length]; // Weights of words in uniqueWords
        int friendIndex;
        try { // To catch if fileName doesn't open
            String line = null;
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                /* For each existing word in the file fileName, check if it corresponds to the current word,
                * then checks frequency value */
                line = " " + line;
                for(int i = 0; i < uniqueWords.length; i++) {
                    if(line.contains(" " + uniqueWords[i]+" ")) {
                        String tmp = line.substring(line.indexOf(uniqueWords[i]) + uniqueWords[i].length() + 1);
                        weightsUnique[i]= 1/Double.parseDouble(tmp);
                        int index = Arrays.asList(sentWords).indexOf(uniqueWords[i]);
                        if (index>=0) {
                            //System.out.println("Word in unique: \"" + uniqueWords[i] + "\" is same as \"" + sentWords[index]+"\" in sentence");
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
                                System.out.println("Word in unique: \"" + uniqueWords[i] + "\" is closest to " +
                                        "\"" + friendWord+"\" in sentence, and sim is " + sim.get(0)[i]);
                                System.out.println("Length of weight: " + weightsSent.length);
                                System.out.println("Look at open: "+ index);
                                System.out.println("Length of sim: "+sim.get(0).length);
                                System.out.println("Friends in sim: "+Arrays.toString(sim.get(0)));
                                System.out.println("Sentence: \""+sent+"\"");
                                System.out.println("Unique: \""+unique+"\"");
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

    // Calculate similarity between the sentences, with weighted words.
    public double wordSimilarity(double[] vec1, double[] vec2, List<double[]> weights1, List<double[]> weights2) {
        double simSum = 0;
        double vec1Sum = 0;
        double vec2Sum = 0;
        double w1;
        double w2;
        boolean notYet = true;
        if(Double.isNaN(simSum)) {
            System.out.println("SimSum is NaN ALL ALONG");
        }
        for(int i = 0; i < vec1.length; i++){
            if(vec1[i]!=Double.NEGATIVE_INFINITY && vec2[i]!=Double.NEGATIVE_INFINITY) {
                w1 = weights1.get(0)[i] * weights1.get(1)[i];
                w2 = weights2.get(0)[i] * weights2.get(1)[i];
                simSum += vec1[i] * vec2[i] * w1 * w2;
                vec1Sum += vec1[i] * vec1[i] * w1 * w1;
                vec2Sum += vec2[i] * vec2[i] * w2 * w2;
                if (Double.isNaN(simSum)&&notYet) {
                    System.out.println("SimSum is "+simSum+" for index " + i + " of " + vec1.length + " total");
                    System.out.println(weights1.get(0)[i] + " " + weights1.get(1)[i] + " " + weights2.get(0)[i] + " " + weights2.get(1)[i] + " ");
                    System.out.println(vec1[i] + " " + vec2[i]);
                    notYet = false;
                }
            }
        }

        if(Double.isNaN(simSum)) {
            //System.out.println("SimSum is NaN");
        }
        if(Double.isNaN(simSum/(Math.sqrt(vec1Sum)*Math.sqrt(vec2Sum)))) {
            //System.out.println("Sim is NaN");
        }
        if(Math.sqrt(vec1Sum)*Math.sqrt(vec2Sum)== 0) {
            //System.out.println("Divide by zero");
        }

        return simSum/(Math.sqrt(vec1Sum)*Math.sqrt(vec2Sum));
    }


    public double orderSimilarity(List<double[]> s1, List<double[]> s2, List<double[]> weights1,
                                  List<double[]> weights2) {
        double[] s1Dist = s1.get(0);
        double[] s1Friend = s1.get(1);
        double[] s2Dist = s2.get(0);
        double[] s2Friend = s2.get(1);
        double[] r1 = new double[s1Dist.length];//r1 and r2 are the same length
        double[] r2 = new double[s2Dist.length];
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
        //System.out.println("R1: "+ Arrays.toString(r1)+ " \nR2: "+Arrays.toString(r2));
        double numerator = 0.0;
        double denominator = 1.0;
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
    public List<double[]> CreateWordVector(String sent){
        List<double[]> wordVecs = new ArrayList<double[]>();
        String[] splitSent = sent.split(" ");
        for(int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);
            if (wordVector[0]!=-100) {
                wordVecs.add(wordVector);
            }
            else{
                //System.out.println("Word "+splitSent[i] +" NOT FOUND");
            }
        }
        return wordVecs;

    }

}