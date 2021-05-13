package hec.stats;

import java.util.Arrays;

public class MedianComputable implements Computable {
    @Override
    public float compute(float[] values) {
        //calculate the min of values
        int n = values.length;
        Arrays.sort(values);
        if(n % 2 !=0) {
            return values[n /2];
        }
        return (values[(n-1)/2] + values[n /2]) / 2;
    }
}
