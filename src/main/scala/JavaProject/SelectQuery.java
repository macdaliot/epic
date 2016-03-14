package JavaProject;

import epic.sequences.SemiCRF;

import java.io.*;
import java.util.*;

public class SelectQuery {

    public Batch SelectQuery(File fileName, int batchSize, String modelChoice, List<SemiCRF<String,String>> models,
                             double threshold, List<List<Double>> informationDensities) {
        List<Double> bestValues = new ArrayList<Double>();
        List<Double> randomIDs = new ArrayList<Double>();
        List<String> bestSentences = new ArrayList<String>();
        double confidenceSum = 0;
        double maxConf = 0;
        double minConf = -1000;
        String maxLine="";
        String minLine="";
        List<Double> ids = new ArrayList<>();
        List<Double> densities = new ArrayList<>();
        if (informationDensities.size()>0){
            ids = informationDensities.get(0);
            densities = informationDensities.get(1);
        }


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
            double c = 0.0;
            long startTime = System.currentTimeMillis();
            System.out.println("**********BATCH SIZE INSIDE***********");

            while ((line = bufferedReader.readLine()) != null) {
                c++;
                //System.out.println("c is "+ c);
                if(c/1000 == Math.floor(c/1000))
                {
                    System.out.println(c/1000 + "takes " + (System.currentTimeMillis()-startTime)/1000 + "s");
                    System.out.println("Average vote value: "+confidenceSum/c);
                }

                String randomID = line.substring(line.indexOf("u'random':") + 11);
                randomID = randomID.substring(0, randomID.indexOf(", u'"));
                String tmpLine = line.substring(line.indexOf("sentence': u") + 13);
                tmpLine = tmpLine.substring(0, tmpLine.indexOf(", u'")-1);
                //System.out.println("tmp to LC is " + tmpLine);
                tmpLine = tmpLine.replaceAll("\\s+", " ");
                tmpValue = MethodChoice.getValueMethod(models, modelChoice, tmpLine);
                confidenceSum += tmpValue;
                if (informationDensities.size()>0){
                    index = ids.indexOf(Double.parseDouble(randomID));
                    if (index!=-1) {
                        tmpValue = tmpValue * densities.get(index);
                    }
                }
                //System.out.println("LC value is " + tmpValue);
                if(tmpValue< maxConf){maxConf = tmpValue; maxLine = tmpLine;}
                if(tmpValue> minConf){minConf = tmpValue; minLine = tmpLine;}
                tmpRandomID = Double.parseDouble(randomID);
                String conll = line.substring(line.indexOf("u'conll': u'") + 12);
                conll = conll.substring(0, conll.indexOf(", u'"));
                if (counter <= batchSize && !bestValues.contains(tmpValue)) {
                    bestValues.add(tmpValue);
                    randomIDs.add(tmpRandomID);
                    bestSentences.add(tmpValue + ", " +tmpLine);
                    counter++;
                } else {
                    minValue = Collections.min(bestValues);
                    minIndex = bestValues.indexOf(minValue);
                    if (minValue < tmpValue && !bestValues.contains(tmpValue)) {
                        bestValues.set(minIndex, tmpValue);
                        randomIDs.set(minIndex, tmpRandomID);
                        bestSentences.set(minIndex,tmpValue + " " +tmpLine);
                    }
                }


            }
            if(threshold>0){
                return thresholdBatch(bestValues, randomIDs, bestSentences, threshold);
            }
            System.out.println("Average vote value: "+confidenceSum/c);

            System.out.println("**********NUMBER OF ITERATIONS IN SQ*********** "+ c);
            System.out.println("**********NUMBER OF COUNTER IN SQ*********** "+ counter);

            int loop = randomIDs.size();
            if (loop>5) {
            loop = 5;
            }
            for (int i = 0; i < loop; i++) {
                System.out.println("**********The best value is: " + bestValues.get(i) + "\n  and has id " + randomIDs.get(i)+"*******");
                System.out.println(bestSentences.get(i));
            }
            for (int i = 0; i < randomIDs.size(); i++) {
                System.out.println("All values: " + bestValues.get(i));
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
        /*System.out.println("Before normalization");
        System.out.println(Arrays.toString(bestValues.toArray()));
        for(int i = 0; i<bestValues.size(); i++){
            bestValues.set(i,(bestValues.get(i)-minConf)/Math.abs(maxConf-minConf));
        }
        System.out.println("After normalization");*/
        //System.out.println(Arrays.toString(bestValues.toArray()));
        //System.out.println("maxConf " + maxConf + " minConf " + minConf);
        //System.out.println("maxLine: \"" + maxLine + " \"");
        //System.out.println("minLine: \"" + minLine + " \"");
            try {
                PrintWriter pw = new PrintWriter("data/stuff.txt");
                for (int i = 0; i < randomIDs.size(); i++) {
                    pw.write(bestValues.get(i)+" "+bestSentences.get(i).split(" ").length+"\n");
                }
                pw.close();
            } catch(IOException fe){ System.out.println(fe);}

        return new Batch(bestSentences,randomIDs,bestValues);
    }

    public Batch thresholdBatch(List<Double> bestValues, List<Double> randomIDs, List<String> bestSentences, double threshold){
        List<Double> sortedIds = new ArrayList<>();
        List<Double> sortedValues = new ArrayList<>(bestValues);
        List<String> sortedSentences = new ArrayList<>();
        Collections.sort(sortedValues);
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
            System.out.println("Sum: " +sum +" added value: "+ sortedValues.get(i) + " for i "+i);
            i--;
        }
        //System.out.println("Ids: "+ Arrays.toString(sortedIds.toArray()));
        //System.out.println("Values: "+ Arrays.toString(sortedValues.toArray()));
        //System.out.println("Sentences: "+ Arrays.toString(sortedSentences.toArray()));
        return new Batch(threshSentences,threshIds,threshValues);

    }
}