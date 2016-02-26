package JavaProject;

import java.util.*;
import java.io.*;
import java.lang.*;

public class EuclidianDistance {


    public double EuclidianDistance(double[] v1, double[] v2) {

        double norm = 0;
        System.out.println();
        for(int i = 0; i < v1.length; i++) {
            norm = norm + (v1[i] - v2[i]) * (v1[i] - v2[i]);
            System.out.println("v1 "+ v1[i]+ " v2 "+ v2[i]);
        }
        norm = Math.sqrt(norm);

        return norm;
    }

}