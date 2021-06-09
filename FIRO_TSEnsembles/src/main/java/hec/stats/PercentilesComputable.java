package hec.stats;

import java.util.Arrays;

public class PercentilesComputable implements Computable {
    private float[] _percentiles;

    /**
     * Instantiates a percentile computable object
     * @param percentile is expected to be in decimal
     */

    public PercentilesComputable(float percentile) {
        this._percentiles = new float[] {percentile};
    }

    public PercentilesComputable(float[] floats) {
        this._percentiles = floats;
    }

    @Override
    public float compute(float[] values) {
        return computePercentile(values, _percentiles[0]);
    }

    private float linInterp(float x1, float x2, float y1, float y2, float p) {
        //linear interpolation to estimate the value given the exceedance

        double slp = (y2 - y1) / (x2 - x1);
        double interpValue = slp * (p -x1) + y1;
        return (float) interpValue;
    }

    public Double[] computeMulti(float[] values) {
        int size = this._percentiles.length;
        Double[] result = new Double[size];
        int i = 0;

        for (float p: this._percentiles) {
            result[i] = Double.valueOf(computePercentile(values, p));
            i++;
        }

        return result;
    }

    private float computePercentile(float[] values, float p) {
        if (p > 1.0) {
            throw new ArithmeticException("Percentile must be less than equal to 1");
        }
        if (p < 0) {
            throw new ArithmeticException("Percentile must be greater than or equal to 0");
        }
        //sorts array
        Arrays.sort(values);

        if (p == 0) {
            return (float) values[0];
        } else {
            if (p == 1.0) {
                return (float) values[values.length - 1];
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
}
