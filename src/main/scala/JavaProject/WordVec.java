package JavaProject;

import java.util.Arrays;
import java.util.List;

public class WordVec
{
    private final List<String> words;
    private final List<double[]> vectors;

    public WordVec(List<String> words, List<double[]> vectors) {
        this.words = words;
        this.vectors = vectors;
    }

    public List<String> getWords() {
        return words;
    }

    public List<double[]> getVectors() {
        return vectors;
    }

    public double[] getVector(int i) {return vectors.get(i);}

    public String getWord(int i) {
        return words.get(i);
    }

    public double[] getVectorOfWord(String word) {

        int index = words.indexOf(word);
        if (index>=0) {
            return vectors.get(index);
        }
        else {
            double vector[] = new double[300];
            Arrays.fill(vector,-100.0);
            return vector;
        }
    }

    public int getSize() { words.size()}
}