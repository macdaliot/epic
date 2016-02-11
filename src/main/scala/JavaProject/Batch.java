package JavaProject

import java.util.*;
import java.io.*;
import java.lang.Object;

public class Batch
{
    private final List<String> sentences;
    private final List<Double> ids;

    public Batch(List<String> sentences, List<Double> ids) {
        this.sentences = sentences;
        this.ids = ids;
    }

    public List<String> getSentences() {
        return sentences;
    }

    public List<Double> getIds() {
        return ids;
    }
}