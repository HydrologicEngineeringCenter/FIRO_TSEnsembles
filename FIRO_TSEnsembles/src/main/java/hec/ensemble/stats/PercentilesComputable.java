package hec.ensemble.stats;

import java.util.Arrays;

public class PercentilesComputable implements Computable, MultiComputable {
    private float[] _percentiles;

    /**
     * Instantiates a percentile computable object
     * @param percentile is expected to be in decimal
     */

    public PercentilesComputable(float percentile) {
        this._percentiles = new float[] {percentile};
    }
    //empty constructor added to satisfy <init>() function deserializing from XML with reflection
    public PercentilesComputable(){}

    public PercentilesComputable(float[] percentiles) {
        this._percentiles = percentiles;
    }

    @Override
    public float compute(float[] values) {
        Arrays.sort(values);
        return computePercentile(values, _percentiles[0]);
    }

    @Override
    public float[] multiCompute(float[] values) {
        int size = this._percentiles.length;
        float[] result = new float[size];
        int i = 0;
        Arrays.sort(values);

        for (float p: this._percentiles) {
            result[i] = Float.valueOf(computePercentile(values, p));
            i++;
        }
        return result;
    }
    /**
     * computePercentile must be sorted.
     * @param values must be sorted
     * @param p is the percentile value
     */

    private float computePercentile(float[] values, float p) {
        if (p > 1.0) {
            throw new ArithmeticException("Percentile must be less than equal to 1");
        }
        if (p < 0) {
            throw new ArithmeticException("Percentile must be greater than or equal to 0");
        }
        //sorts array
        //Arrays.sort(values);

        if (p == 0) {
            return values[0];
        } else {
            if (p == 1.0) {
                return values[values.length - 1];
            } else {
                int startIndex = (int) (p * (values.length-1));
                int endIndex = startIndex + 1;

                float x1 = (float) (startIndex) / (values.length - 1);
                float x2 = (float) (endIndex) / (values.length -1);
                float y1 = values[startIndex];
                float y2 = values[endIndex];
                float val = linInterp(x1, x2, y1, y2, p);
                return val;
            }
        }
    }
    private float linInterp(float x1, float x2, float y1, float y2, float p) {
        //linear interpolation to estimate the value given the exceedance

        double slp = (y2 - y1) / (x2 - x1);
        double interpValue = slp * (p -x1) + y1;
        return (float) interpValue;
    }

    @Override
    public Statistics[] Statistics() {
        Statistics[] ret = new Statistics[_percentiles.length];
        for (int i = 0 ; i < _percentiles.length; i ++){
            ret[i] = Statistics.PERCENTILE;
        }
        return ret;
    }

    @Override
    public String StatisticsLabel() {
        String label = "";
        for (int i = 0 ; i < _percentiles.length; i ++){
            if(i == _percentiles.length-1){
                label += Statistics.PERCENTILE + "(" +_percentiles[i] + ")";
            }
            else{
                label += Statistics.PERCENTILE + "(" +_percentiles[i] + ")|";
            }

        }
        return label;
    }
}
