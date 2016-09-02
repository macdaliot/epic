import epic.sequences.SemiCRF;
import epic.sequences.SemiConllNerPipeline;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InfoDensTester {


    public static void main(String[] args) {
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
        WordVec allWordsVec = InformationDensity.createWordVec(args[0], 300);
        File fileNameWordFreq = new File("/Users/" + args[0] + "/Dropbox/Exjobb/PythonThings/wordFreq.txt");
        double similarities[];
        double delta = Double.parseDouble(args[1]);
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
        String objectSentence;
        String pairSentence;
        if (args.length>2){
            objectSentence = args[2].toLowerCase();
            pairSentence = args[3].toLowerCase();
            wordVecs1 = InformationDensity.createSentenceWordVector(objectSentence, allWordsVec);
            wordVecs2 = InformationDensity.createSentenceWordVector(pairSentence, allWordsVec);
            similarities = cs.CalculateSimilarity(objectSentence, pairSentence, wordFreqs, allWordsVec, wordVecs1, wordVecs2);
            System.out.println(similarities[0] * delta + (1-similarities[1]) * (1 - delta));

        }


        for (int i = 0; i < sentence1.length; i++) {
            objectSentence = sentence1[i].toLowerCase();
            objectSentence = objectSentence.replaceAll("\\p{Punct}+", "");
            wordVecs1 = InformationDensity.createSentenceWordVector(objectSentence, allWordsVec);


            pairSentence = sentence2[i].toLowerCase();
            pairSentence = pairSentence.replaceAll("\\p{Punct}+", "");
            wordVecs2 = InformationDensity.createSentenceWordVector(pairSentence, allWordsVec);
            similarities = cs.CalculateSimilarity(objectSentence, pairSentence, wordFreqs, allWordsVec, wordVecs1, wordVecs2);

            System.out.println(similarities[0] * delta + (1-similarities[1]) * (1 - delta));



        }
    }
}