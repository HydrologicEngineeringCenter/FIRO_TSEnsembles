package hec.ensemble.stats;

import java.util.Arrays;

public class MedianComputable implements Computable {
    @Override
    public float compute(float[] values) {
        int n = values.length;
        Arrays.sort(values);
        if(n % 2 !=0) {
            return values[(n) /2];
        }
        return (values[(n)/2-1] + values[n /2]) / 2;
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MEDIAN};
    }
}
