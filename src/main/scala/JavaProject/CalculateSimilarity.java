package JavaProject;

import java.io.*;
import java.util.*;
import java.lang.Object;
import java.lang.*;

public class CalculateSimilarity
{
    int vectorLength = 5;
    public double threshold = 0.2;
    public WordVec allWordsVec;

    public void CalculateSimilarity(String sent1,String sent2, File fileName, WordVec allWordsVec) {
        this.allWordsVec = allWordsVec;
        sent1 = sent1.toLowerCase();
        sent2 = sent2.toLowerCase();
        CosSim cs = new CosSim();
        String uniqueWords = uniqueWordSentence(sent1, sent2);
        List<double[]> uniqueWordVecs = CreateWordVector(uniqueWords);
        uniqueWords = getFoundWords(uniqueWords);
        sent1 = getFoundWords(sent1);
        sent2 = getFoundWords(sent2);
        List<double[]> wordVecs1 = CreateWordVector(sent1);
        List<double[]> wordVecs2 = CreateWordVector(sent2);
        List<double[]> wordSim1 = similarityVectors(wordVecs1, uniqueWordVecs, cs);
        List<double[]> wordSim2 = similarityVectors(wordVecs2,uniqueWordVecs, cs);
        List<double[]> weights1 = WordWeights(fileName, sent1, uniqueWords, wordSim1);
        List<double[]> weights2 = WordWeights(fileName, sent2, uniqueWords, wordSim2);
        double wordSimilarityScore = wordSimilarity(wordSim1.get(0),wordSim2.get(0),weights1, weights2);
        double orderSimilarityScore = orderSimilarity(wordSim1, wordSim2, weights1,
                weights2, wordVecs1, wordVecs2, uniqueWordVecs);


        /*for(int i = 0; i< 6; i++){
            System.out.println("Word "+ allWordsVec.getWord(i));
            System.out.println("Vector "+ Arrays.toString(allWordsVec.getVector(i)));
        }*/
        /*System.out.println("word similarityf jscor "+ wordSimilarityScore);

        System.out.println("order similarityf jscor "+ orderSimilarityScore);
        for(int i = 0; i< wordSim1.size(); i++){
            System.out.println("wordSim1 "+ Arrays.toString(wordSim1.get(i)));
        }
        for(int i = 0; i< wordSim2.size(); i++){
            System.out.println("wordSim2 "+ Arrays.toString(wordSim2.get(i)));
        }
        for(int i = 0; i< weights1.size(); i++){
            System.out.println("weights1 "+ Arrays.toString(weights1.get(i)));
        }
        for(int i = 0; i< weights2.size(); i++){
            System.out.println("weights2 "+ Arrays.toString(weights2.get(i)));
        }

        System.out.println("unique words are  "+ uniqueWords);

        for(int i = 0; i< uniqueWordVecs.size(); i++){
            System.out.println("uniqueWordVecs "+ Arrays.toString(uniqueWordVecs.get(i)));
        }*/
    }

    // Creates a list of vectors where each instance in the list corresponds to a word in the sentence sent,
    // and each vector the vector representation of that word.
    public List<double[]> CreateWordVector(String sent){
        List<double[]> wordVecs = new ArrayList<double[]>();
        String[] splitSent = sent.split(" ");
        System.out.println("sent.split().length is " +sent.split(" ").length);
        for(int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);
            if (wordVector[0]!=-100) {
                wordVecs.add(wordVector);
            }
            else{
                System.out.println("Word "+splitSent[i] + " in sentence " + sent);
            }
        }
        System.out.println("Size of sentence: \""+sent+"\" is " +wordVecs.size());
        return wordVecs;

    }

    public String getFoundWords(String sent){
        String foundUniq = "";
        String[] splitSent = sent.split(" ");
        System.out.println("sent.split().length is " +sent.split(" ").length);
        for(int i = 0; i < splitSent.length; i++) // For each word
        {
            double[] wordVector = allWordsVec.getVectorOfWord(splitSent[i]);
            if (wordVector[0]!=-100) {
                foundUniq += splitSent[i]+" ";
            }
            else{
                System.out.println("Word "+splitSent[i] + " in sentence " + sent + " NOT FOUND");
            }
        }
        System.out.println("Uniq Sentence is: "+ foundUniq);
        return foundUniq;

    }


    // Finds which word in all the uniqueWords that a word from the sentence (wordVecs) is closest to,
    // also saves the index in uniqueWords that corresponds to this word.
    public List<double[]> similarityVectors(List<double[]> wordVecs,List<double[]> uniqueWordVecs, CosSim cs){
        double[] shortestDistances = new double[uniqueWordVecs.size()];
        double[] bestFriends = new double[uniqueWordVecs.size()]; //Index of closest word
        int friend = 0;
        for(int i = 0; i < uniqueWordVecs.size();i++) // For all unique words
        {
            double currentShortest = Double.POSITIVE_INFINITY;
            for(int j = 0; j <wordVecs.size();j++) // Finds closest word in wordVecs
            {
                double tmpDist = cs.CosSim(wordVecs.get(j), uniqueWordVecs.get(i));
                //System.out.println("vector sizes" + wordVecs.get(j).length + " "+ uniqueWordVecs.get(j).length);
                //System.out.println("tmpDist at " + j+ " is " +tmpDist);
                if(tmpDist> currentShortest)
                {
                    currentShortest = tmpDist;
                    friend = j;

                }

            }
            shortestDistances[i] = currentShortest;
            //System.out.println("Friend is set to " + friend);
            bestFriends[i] = friend;

        }

        List<double[]> results = new ArrayList<double[]>();
        results.add(shortestDistances);
        results.add(bestFriends);
        System.out.println("Sim size is " + results.get(0).length);
        return results;
    }


    // Finds the weights of a word, both concerning the weight of the word itself, and its closest friend in
    // the unique words. The weights are inversly proportional to the frequency of the word
    // Frequences of words are found in fileName
    public List<double[]> WordWeights(File fileName, String sent, String unique, List<double[]> sim ){
        String[] sentWords = sent.split(" ");
        String[] uniqueWords = unique.split(" ");
        System.out.println("******* WordWeights ********");
        System.out.println("uniquWords is: "+Arrays.toString(uniqueWords));
        System.out.println("SentWords is: "+Arrays.toString(sentWords));
        System.out.println("Shortest dist vec is: "+Arrays.toString(sim.get(0)));
        System.out.println("Friend vec is: "+Arrays.toString(sim.get(1)));

        double[] weightsSent = new double[uniqueWords.length]; // Weights of closest words in sent to words in uniqueWords
        double[] weightsUnique = new double[uniqueWords.length]; // Weights of words in uniqueWords
        int friendIndex;
        try { // To catch if fileName doesn't open
            String line = null;
            FileReader fileReader = new FileReader(fileName);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            System.out.println(Arrays.toString(uniqueWords));
            while ((line = bufferedReader.readLine()) != null) {
                /* For each existing word in the file fileName, check if it corresponds to the current word,
                * then checks frequency value */
                line = " " + line;
                for(int i = 0; i < uniqueWords.length; i++) {
                    //System.out.println(line + " " +i);
                    if(line.contains(" " + uniqueWords[i]+" ")) {
                        String tmp = line.substring(line.indexOf(uniqueWords[i]) + uniqueWords[i].length() + 1);
                        weightsUnique[i]= 1/Double.parseDouble(tmp);
                        int index = Arrays.asList(sentWords).indexOf(uniqueWords[i]);
                        if (index>=0) {
                            weightsSent[index] = 1 / Double.parseDouble(tmp);
                        }
                        else if(sim.get(0)[i]>threshold){
                            weightsSent[(int) sim.get(1)[i]]= 1 / Double.parseDouble(tmp);
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
        for(int i = 0; i < vec1.length; i++){
            w1 = weights1.get(0)[i]*weights1.get(1)[i];
            w2 = weights2.get(0)[i]*weights2.get(1)[i];
            simSum += vec1[i]*vec2[i]*w1*w2;
            vec1Sum += vec1[i]*vec1[i]*w1*w1;
            vec2Sum += vec2[i]*vec2[i]*w2*w2;
        }
        return simSum/(Math.sqrt(vec1Sum)*Math.sqrt(vec2Sum));
    }


    public double orderSimilarity(List<double[]> s1, List<double[]> s2, List<double[]> weights1,
                                  List<double[]> weights2, List<double[]> wordVecs1,
                                  List<double[]> wordVecs2, List<double[]> uniqueWordVecs) {
        double[] s1Dist = s1.get(0);
        double[] s1Friend = s1.get(1);
        double[] s2Dist = s2.get(0);
        double[] s2Friend = s2.get(1);
        double[] r1 = new double[s1Dist.length];//r1 and r2 are the same length
        double[] r2 = new double[s2Dist.length];
        System.out.println("s1 " + Arrays.toString(s1Dist) + "\ns2 "+Arrays.toString(s2Dist));
        for(int i =0; i< r1.length;i++){
            if(s1Dist[i]==1.0){
                r1[i]=i;
            }else if(s1Dist[i]>=threshold) {
                r1[i] = s1Friend[i];

            }else{
                r1[i]=0.0;
            }

        }
        for(int i =0; i< r2.length;i++){
            if(s2Dist[i]==1.0){
                r2[i]=i;
            }else if(s2Dist[i]>=threshold) {
                r2[i] = s2Friend[i];

            }else{
                r2[i]=0.0;
            }

        }

        System.out.println("r1 " + Arrays.toString(r1) + "\nr2 "+Arrays.toString(r2));
        double numerator = 0.0;
        double denominator = 1.0;
        for(int i = 0; i< r1.length; i ++){
            numerator = numerator + Math.pow((r1[i]-r2[i])*weights1.get(1)[i]*weights2.get(1)[i],2);
            denominator = denominator + Math.pow((r1[i]+r2[i])*weights1.get(1)[i]*weights2.get(1)[i],2);
        }
        numerator = Math.sqrt(numerator);
        denominator = Math.sqrt(denominator);

        return numerator/denominator;
    }

    public String uniqueWordSentence(String s1, String s2)
    {
        String test = s1.concat(" "+s2);
        //Set<String> unique = new HashSet<String>(Arrays.asList(test.toLowerCase().split("(`~!@#$%^&*()_+|:\"<>?-[];\'./, 0123456789\\s)+")));
        Set<String> unique = new HashSet<String>(Arrays.asList(test.toLowerCase().split("\\W+")));
        System.out.println(test + " is of length " +unique.size());
        Iterator it = unique.iterator();
        String tmp = it.next().toString();
        while (it.hasNext()){
            tmp = tmp.concat(" " +it.next().toString());
        }
        return tmp;

    }

}