package JavaProject;

public class CosSim {

    /**
     * Calculates the cosine similarity of two vectors of any length
     *
     * @param v1 Vector 1
     * @param v2 Vector 2
     * @return a double representing the cosine similarity.
     */

    public double CosSim(double[] v1, double[] v2) {

        double norm1 = 0;
        double norm2 = 0;
        double scalar = 0;
        for (int i = 0; i < v1.length; i++) {
            scalar += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        if (v1[0] == -100 | v2[0] == -100) {
            return Double.NEGATIVE_INFINITY;
        } else if (norm1 * norm2 == 0) {
            return 0;
        }
        scalar = scalar / (norm1 * norm2);
        if (Math.abs(scalar - 1) < 0.001)
            return 1;
        else if (Math.abs(scalar) < 0.001)
            return 0;
        else
            return scalar;
    }

}