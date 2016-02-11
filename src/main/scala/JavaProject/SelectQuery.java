package JavaProject

import java.io.*;
import java.util.*;

public class SelectQuery {

    public Batch SelectQuery(File fileName, int batchSize, int modelChoice, String modelFileName) {
        List<Double> bestValues = new ArrayList<Double>();
        List<Double> randomValues = new ArrayList<Double>();
        List<String> bestSentences = new ArrayList<String>();
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
            double tmpRandom;
            double maxValue = Double.NEGATIVE_INFINITY;
            int counter = 1;

            while ((line = bufferedReader.readLine()) != null) {
                String tmp = line.substring(line.indexOf("u'random':") + 11);
                tmp = tmp.substring(0, tmp.indexOf(", u'"));
                tmpValue = ModelChoice.getValueModel(modelFileName, modelChoice, tmp);
                tmpRandom = Double.parseDouble(tmp);
                if (counter <= batchSize) {
                    bestValues.add(tmpValue);
                    tmp = line.substring(line.indexOf("u'conll': u'") + 12);
                    tmp = tmp.substring(0, tmp.indexOf("', u'"));
                    bestSentences.add(tmp);
                    randomValues.add(tmpRandom);
                } else {
                    minValue = Collections.min(bestValues);
                    minIndex = bestValues.indexOf(minValue);
                    if (minValue < tmpValue) {
                        tmp = line.substring(line.indexOf("u'conll': u'") + 12);
                        tmp = tmp.substring(0, tmp.indexOf("', u'"));
                        bestSentences.set(minIndex, tmp);
                        bestValues.set(minIndex, tmpValue);
                        randomValues.set(minIndex, tmpRandom);
                        for (int i = 0; i < batchSize; i++) {
                            //System.out.println(bestValues.get(i));
                        }
                        //System.out.println("Get outta here!");
                    }
                }
                counter++;

            }
            for (int i = 0; i < batchSize; i++) {
                //System.out.println(bestSentences.get(i) + "\n has random value " + randomValues.get(i));
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

        Batch currentBatch = new Batch(bestSentences, randomValues);
        return currentBatch;
    }
}