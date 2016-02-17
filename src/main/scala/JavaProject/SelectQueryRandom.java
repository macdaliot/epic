package JavaProject;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SelectQueryRandom {

    public List<Double> SelectQueryRandom(File fileName, int batchSize) {
        List<Double> randomIDs = new ArrayList<Double>();
        try {
            // This will reference one line at a time
            String line;

            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            //System.out.println("I'm in SelectQuery");
            double c = 0.0;
            int index = 0;
            int size = 0;//Antalet unlabeled
            while ((bufferedReader.readLine()) != null) {size++;}
            ArrayList<Integer> list = new ArrayList<Integer>(size);
            for(int i = 0; i < size; i++) {
                list.add(i);
            }
            // shuffle the list
            Collections.shuffle(list);
            //Take out the first 100
            ArrayList<Integer> randomIndices = new ArrayList<Integer>(list.subList(0, batchSize-1));
            while ((line = bufferedReader.readLine()) != null) {
                c++;
                if(c/1000 == Math.floor(c/1000))
                {
                    System.out.println(c/1000);
                }
                if (Arrays.asList(randomIndices).contains(index)) {
                    String randomID = line.substring(line.indexOf("u'random':") + 11);
                    randomID = randomID.substring(0, randomID.indexOf(", u'"));
                    double tmpRandomID = Double.parseDouble(randomID);
                    randomIDs.add(tmpRandomID);
                }
                index++;
            }


            int loop = randomIDs.size();
            if (loop>5) {
            loop = 5;
            }


            for (int i = 0; i < loop; i++) {
                System.out.println("Random ids: " + randomIDs.get(i));
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

        return randomIDs;
    }
}