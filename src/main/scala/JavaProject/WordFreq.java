package JavaProject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WordFreq
{
    private final String word;
    private final Double freq;

    /**
     * Batch object contains a batch of sentences
     * @param word A word
     * @param freq The words frequency
     */
    public WordFreq(String word,Double freq ) {
        this.word = word;
        this.freq = freq;
    }


    public String getWord() {
        return word;
    }
    public Double getFreq() {
        return freq;
    }
}