package JavaProject;

import epic.sequences.SemiCRF;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SelectQueryGet10k {

    public List<String> SelectQueryGet10k(File fileName, int batchSize, int modelChoice, SemiCRF<String,String> model) {
        List<Double> bestValues = new ArrayList<Double>();
        List<Double> randomIDs = new ArrayList<Double>();
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
            double tmpRandomID;
            double maxValue = Double.NEGATIVE_INFINITY;
            int counter = 1;
            //System.out.println("I'm in SelectQuery");
            double c = 0.0;

            while ((line = bufferedReader.readLine()) != null) {
                c++;
                System.out.println(c);
                if(c/1000 == Math.floor(c/1000))
                {
                    System.out.println(c/1000);
                }
                String[] splitLine = line.split(" ");
                String sentence = "";
                if (splitLine.length>2) {
                    for (int i = 2; i < splitLine.length - 1; i++) {
                        sentence += splitLine[i] + " ";
                    }
                    sentence += splitLine[splitLine.length - 1];
                    //System.out.println(sentence);
                    sentence = sentence.replaceAll("\\s+", " ");
                    tmpValue = ModelChoice.getValueModel(model, modelChoice, sentence);
                    if (counter <= batchSize) {
                        bestValues.add(tmpValue);
                        bestSentences.add(line.length() + " " + tmpValue);
                    } else {
                        minValue = Collections.min(bestValues);
                        minIndex = bestValues.indexOf(minValue);
                        if (minValue < tmpValue) {
                            bestValues.set(minIndex, tmpValue);
                            bestSentences.set(minIndex, line.length() + " " + tmpValue);
                        }
                    }
                    counter++;
                }

            }


            int loop = bestSentences.size();
            if (loop>5) {
            loop = 5;
            }


            for (int i = 0; i < loop; i++) {
                System.out.println("The best value is: " + bestValues.get(i) + "\n  for sentence: " + bestSentences.get(i));
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

        return bestSentences;
    }
}