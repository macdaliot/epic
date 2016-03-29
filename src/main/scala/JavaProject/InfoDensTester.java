package JavaProject;

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
        String[] sentence1 = {"I like that malware","I like that malware","Is that an attack?",
                "This office is where I am seated","Intruders are dangerous",
                "Intruders are dangerous", "A malware attack","There is a bug in my hair",
                "Fear the malicious robot","You are safe from danger under my aegis",
                "Cat is happy","Cat is happy","Cat is happy","Get to the malware","Get to the malware"};
        String[] sentence2 = {"I like that virus","I like that banana","That is an attack","I am here",
                "Criminals pose a danger","That is a nice panda","Kitten beauty cafe","My computer has a bug",
                "Malwares are to be feared","I protect from all menaces","Hacking is harder",
                "Kitten is pleased","Panda does yoga","Robot destroys underworld","Undulate towards malignancy","Gett too teh mawlare"};

    }
}