import epic.sequences.SemiCRF;

import java.io.*;
import java.util.*;

public class SelectQuery {

    /**
     * SelectQuery finds the next batch for labeling. It goes through a file and finds the value of the current sentence
     * according to the current model
     * @param fileName The name of the file from which the sentences in the unlabeled pool are to be read
     * @param batchSize The size of the batch that this function is to return
     * @param modelChoice Which kind of active learning is active
     * @param models If we use vote entropy this contains all the models which vote. If not this list contains only
     *               one model
     * @param threshold If we use adaptive batch size this is the threshold which determines how many sentences we send
     *                  back
     * @param informationDensities If information density is used this list contains the similarity scores of each
     *                             sentence
     * @return A Batch which contains the best sentences, their corresponding scores and ids.
     */

    public Batch SelectQuery(File fileName, int batchSize, String modelChoice, List<SemiCRF<String,String>> models,
                             double threshold, List<List<Double>> informationDensities) {
        List<Double> bestValues = new ArrayList<Double>();
        List<Double> randomIDs = new ArrayList<Double>();
        List<String> bestSentences = new ArrayList<String>();
        double confidenceSum = 0;
        List<Double> ids = new ArrayList<>();
        List<Double> densities = new ArrayList<>();
        if (informationDensities.size()>0){
            ids = informationDensities.get(0);
            densities = informationDensities.get(1);
        }

            double positives = 0.0;
            double c = 0.0;
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
                int index;
                System.out.println("I'm in SelectQuery");
                long startTime = System.currentTimeMillis();
                System.out.println("**********BATCH SIZE INSIDE***********");


                while ((line = bufferedReader.readLine()) != null) {
                    c++;
                    if (c / 1000 == Math.floor(c / 1000)) {
                        System.out.println(c / 1000 + "takes " + (System.currentTimeMillis() - startTime) / 1000 + "s");
                        System.out.println("Average vote value: " + confidenceSum / c);
                    }

                    String randomID = line.substring(line.indexOf("u'random':") + 11);
                    randomID = randomID.substring(0, randomID.indexOf(", u'"));
                    String tmpLine = line.substring(line.indexOf("sentence': u") + 13);
                    tmpLine = tmpLine.substring(0, tmpLine.indexOf(", u'") - 1);
                    tmpLine = tmpLine.replaceAll("\\s+", " ");
                    String tmpConll = line.substring(line.indexOf("u'conll': u'") + 12);
                    tmpConll = tmpConll.substring(0, tmpConll.indexOf(", u'"));
                    tmpValue = MethodChoice.getValueMethod(models, modelChoice, tmpLine, tmpConll);
                    if (tmpConll.contains("_MALWARE")){
                        positives++;
                    }
                    confidenceSum += tmpValue;
                    if (informationDensities.size() > 0) {
                        index = ids.indexOf(Double.parseDouble(randomID));
                        if (index != -1) {
                            tmpValue = tmpValue * densities.get(index);
                        }
                    }
                    tmpRandomID = Double.parseDouble(randomID);
                    if (counter <= batchSize && !bestValues.contains(tmpValue)) {
                        bestValues.add(tmpValue);
                        randomIDs.add(tmpRandomID);
                        bestSentences.add(tmpValue + " " + tmpConll);
                        counter++;

                    } else if (!bestValues.contains(tmpValue)) {
                        minValue = Collections.min(bestValues);
                        minIndex = bestValues.indexOf(minValue);
                        if (minValue < tmpValue && !bestValues.contains(tmpValue)) {
                            bestValues.set(minIndex, tmpValue);
                            randomIDs.set(minIndex, tmpRandomID);
                            bestSentences.set(minIndex, tmpValue + " " + tmpConll);
                        }
                    }


                }

                if (threshold > 0) {
                    return thresholdBatch(bestValues, randomIDs, bestSentences, threshold, positives/c);
                }



                int loop = randomIDs.size();
                if (loop > 5) {
                    loop = 5;
                }
                for (int i = 0; i < loop; i++) {
                    System.out.println(bestValues.get(i));
                }

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


        return new Batch(bestSentences,randomIDs,bestValues,positives/c);
    }

    /**
     * If adaptive batch size is active thresholdBatch is called. It loops over a sorted list of values and chooses
     * values until the sum of the values are bigger than the threshold. Hence it returns the best values and their
     * corresponding sentences and ids as a Batch object.
     * @param bestValues The scores of each sentences
     * @param randomIDs The ids of each sentence
     * @param bestSentences each sentence
     * @param threshold The threshold for the adaptive batch size
     * @return A Batch object consisting of the best sentences, their corresponding values and ids.
     */
    public Batch thresholdBatch(List<Double> bestValues, List<Double> randomIDs, List<String> bestSentences, double threshold, double perc){
        List<Double> sortedIds = new ArrayList<>();
        List<Double> sortedValues = new ArrayList<>(bestValues);
        List<String> sortedSentences = new ArrayList<>();
        Collections.sort(sortedValues);
        double positives = 0.0;
        int idIndex;
        for (int i = 0; i < sortedValues.size();i++){
            idIndex = bestValues.indexOf(sortedValues.get(i));
            bestValues.set(idIndex,-1000.0);
            sortedSentences.add(bestSentences.get(idIndex));
            sortedIds.add(randomIDs.get(idIndex));
        }
        double sum = 0;
        int i = sortedValues.size()-1;
        List<Double> threshIds = new ArrayList<>();
        List<Double> threshValues = new ArrayList<>();
        List<String> threshSentences = new ArrayList<>();
        while (sum < threshold && i > -1){
            threshValues.add(sortedValues.get(i));
            threshIds.add(sortedIds.get(i));
            threshSentences.add(sortedSentences.get(i));
            sum += 1+sortedValues.get(i);
            i--;
        }

        return new Batch(threshSentences,threshIds,threshValues,perc);

    }
}