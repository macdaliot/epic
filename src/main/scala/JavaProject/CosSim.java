package JavaProject;

public class CosSim {


    public double CosSim(double[] v1, double[] v2) {

        double norm1 = 0;
        double norm2 = 0;
        double scalar = 0;
        System.out.println();
        for(int i = 0; i < v1.length; i++) {
            scalar += v1[i]*v2[i];
            norm1 += v1[i]*v1[i];
            norm2 += v2[i]*v2[i];
        }
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        if ( norm1*norm2==0 ){
            return -1000;
        }

        return scalar/(norm1*norm2);
    }

}