package JavaProject;

import epic.sequences.SemiCRF;
import epic.sequences.SemiConllNerPipeline;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class InfoDensTester {


    public static void main(String[] args) {
        Properties prop = new Properties();
        String pathToEpic = "";
        try {
            prop.load(new FileInputStream("src/main/resources/config.properties"));
            pathToEpic = prop.getProperty("pathToEpic");

        } catch (IOException ex) {
            System.out.println("Could not find config file. " + ex);
            System.exit(0);
        }
        String[] sentence1 = {"jknsjksjkdfskjs sdsdhdshjsd fsda ","I like that malware", "I like that malware", "Is that an attack?",
                "This office is where I am seated", "Intruders are dangerous",
                "Intruders are dangerous", "A malware attack", "There is a bug in my hair",
                "Fear the malicious robot", "You are safe from danger under my aegis", "Encryption is hard",
                "Cat is happy", "Cat is happy","Cat is happy", "Cat is happy", "Get to the malware", "Get to the malware"};
        String[] sentence2 = {" burt snurt flurt", "I like that virus", "I like that hat", "That is an attack", "I am here",
                "Malware pose a danger", "That is a nice panda", "Kitten beauty cafe", "My computer has a bug",
                "Malwares are to be feared", "I protect from all menaces", "Hacking is harder",
                "Dog is pleased","Kitten is pleased", "Panda does yoga", "Robot destroys underworld", "Undulate towards malignancy", "Gett too teh mawlare"};


        CalculateSimilarity cs = new CalculateSimilarity();
        WordVec allWordsVec = InformationDensity.createWordVec(pathToEpic, 300);
        File fileNameWordFreq = new File(pathToEpic + "/epic/data/wordFreq.txt");
        double similarities[];
        CosSim cossim = new CosSim();
        double delta = Double.parseDouble(args[0]);
        List<WordFreq> wordFreqs = new ArrayList<WordFreq>();
        String line;
        try {
            FileReader fileReader = new FileReader(fileNameWordFreq);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((line = bufferedReader.readLine()) != null) {
                String[] tmp = line.split(" ");
                wordFreqs.add(new WordFreq(tmp[0],Double.parseDouble(tmp[1])));
            }
        }catch(IOException ex){
            System.out.println("Creating wordsFreqs something went wrong: "+ ex);
        }
        List<double[]> wordVecs1;
        List<double[]> wordVecs2;
        String[] objectSentence;
        String[] pairSentence;
        if (args.length>2){
            objectSentence = args[1].toLowerCase().split(" ");
            pairSentence = args[2].toLowerCase().split(" ");
            wordVecs1 = InformationDensity.createSentenceWordVector(args[1].toLowerCase(), allWordsVec);
            wordVecs2 = InformationDensity.createSentenceWordVector(args[2].toLowerCase(), allWordsVec);
            similarities = cs.CalculateSimilarity(objectSentence, pairSentence, wordFreqs, allWordsVec, wordVecs1, wordVecs2,cossim);
            System.out.println(similarities[0] * delta + (1-similarities[1]) * (1 - delta));

        }


        for (int i = 0; i < sentence1.length; i++) {
            objectSentence = sentence1[i].toLowerCase().replaceAll("\\p{Punct}+", "").split(" ");
            wordVecs1 = InformationDensity.createSentenceWordVector(sentence1[i].toLowerCase(), allWordsVec);


            pairSentence = sentence2[i].toLowerCase().replaceAll("\\p{Punct}+", "").split(" ");
            wordVecs2 = InformationDensity.createSentenceWordVector(sentence2[i].toLowerCase(), allWordsVec);
            similarities = cs.CalculateSimilarity(objectSentence, pairSentence, wordFreqs, allWordsVec, wordVecs1, wordVecs2,cossim);

            System.out.println(similarities[0] * delta + (1-similarities[1]) * (1 - delta));



        }
    }
}