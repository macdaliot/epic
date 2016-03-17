package JavaProject;

import java.util.*;
import java.io.*;
import java.lang.*;

public class EuclideanDistance {

    /**
     * Calculates euclidean distance between two vectors of any size
     * @param v1 vector 1
     * @param v2 vector 2
     * @return a double
     */


    public double EuclideanDistance(double[] v1, double[] v2) {

        double norm = 0;
        for(int i = 0; i < v1.length; i++) {
            norm = norm + (v1[i] - v2[i]) * (v1[i] - v2[i]);
            //System.out.println("v1 "+ v1[i]+ " v2 "+ v2[i]);
        }
        norm = Math.sqrt(norm);

        return norm;
    }

}