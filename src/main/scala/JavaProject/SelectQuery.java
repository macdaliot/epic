package JavaProject;

import epic.sequences.SemiCRF;

import java.io.*;
import java.util.*;

public class SelectQuery {

    public Batch SelectQuery(File fileName, int batchSize, String modelChoice, SemiCRF<String,String> model) {
        List<Double> bestValues = new ArrayList<Double>();
        List<Double> randomIDs = new ArrayList<Double>();
        List<String> bestSentences = new ArrayList<String>();
        double confidenceSum = 0;
        double maxConfidence = 0;
        try {
            // This will reference one line at a time
            String line = null;

            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            //Kan behöva läggas till -inf beroende på vilken model vi använder
            //double maxValue = Double.NEGATIVE_INFINITY;
            //String bestSentence;
            double minValue;
            int minIndex;
            double tmpValue;
            double tmpRandomID;
            double maxValue = Double.NEGATIVE_INFINITY;
            int counter = 1;
            System.out.println("I'm in SelectQuery");
            double c = 0.0;

            while ((line = bufferedReader.readLine()) != null) {
                c++;
                //System.out.println("c is "+ c);
                if(c/1000 == Math.floor(c/1000))
                {
                    System.out.println(c/1000);
                }

                String randomID = line.substring(line.indexOf("u'random':") + 11);
                randomID = randomID.substring(0, randomID.indexOf(", u'"));
                String tmpLine = line.substring(line.indexOf("sentence': u") + 13);
                tmpLine = tmpLine.substring(0, tmpLine.indexOf(", u'")-1);
                //System.out.println("tmp to LC is " + tmpLine);
                tmpLine = tmpLine.replaceAll("\\s+", " ");
                tmpValue = MethodChoice.getValueMethod(model, modelChoice, tmpLine);
                confidenceSum += Math.abs(tmpValue);
                if(tmpValue>maxConfidence){
                    maxConfidence=tmpValue;
                }
                //System.out.println("LC value is " + tmpValue);
                tmpRandomID = Double.parseDouble(randomID);
                String conll = line.substring(line.indexOf("u'conll': u'") + 12);
                conll = conll.substring(0, conll.indexOf(", u'"));
                if (counter <= batchSize && !bestValues.contains(tmpValue)) {
                    bestValues.add(tmpValue);
                    randomIDs.add(tmpRandomID);
                    bestSentences.add(tmpValue + " " +conll);
                } else {
                    minValue = Collections.min(bestValues);
                    minIndex = bestValues.indexOf(minValue);
                    if (minValue < tmpValue && !bestValues.contains(tmpValue)) {
                        bestValues.set(minIndex, tmpValue);
                        randomIDs.set(minIndex, tmpRandomID);
                        bestSentences.set(minIndex,tmpValue + " " +conll);
                    }
                }
                counter++;

            }


            int loop = randomIDs.size();
            if (loop>5) {
            loop = 5;
            }


            for (int i = 0; i < loop; i++) {
                System.out.println("The best value is: " + bestValues.get(i) + "\n  and has id " + randomIDs.get(i));
            }
            // Always close files.
            bufferedReader.close();


        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" +
                            fileName + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                            + fileName + "'");
            // Or we could just do this:
            // ex.printStackTrace();
        }
        System.out.println("maxConfidence" + maxConfidence);
        System.out.println("Before normalization" + "confidence sum" + confidenceSum);
        System.out.println(Arrays.toString(bestValues.toArray()));
        for(int i = 0; i<bestValues.size(); i++){
            bestValues.set(i,1+bestValues.get(i)/confidenceSum);
        }
        System.out.println("After normalization");
        System.out.println(Arrays.toString(bestValues.toArray()));
        System.out.println("max conf after norm." + maxConfidence/confidenceSum);

        return new Batch(bestSentences,randomIDs,bestValues);
    }
}