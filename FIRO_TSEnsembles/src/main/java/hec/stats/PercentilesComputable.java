package hec.stats;

import java.util.Arrays;

public class PercentilesComputable implements Computable {
    Double _percentile; //percentile in decimal

    /**
     * Instantiates a percentile computable object
     * @param percentile is expected to be in decimal
     */

    public PercentilesComputable(Double percentile) {
        this._percentile = percentile;
    }

    @Override
    public float compute(float[] values) {
        if (this._percentile > 1.0) {
            throw new ArithmeticException("Percentile must be less than equal to 1");
        }
        if (this._percentile < 0) {
            throw new ArithmeticException("Percentile must be greater than or equal to 0");
        }
        //sorts array
        Arrays.sort(values);

        if (this._percentile == 0) {
            return values[0];
        }
        if (this._percentile == 1.0) {
            return values[values.length-1];
        }

        int startIndex = (int) (this._percentile * (values.length-1));
        int endIndex = startIndex + 1;

        float x1 = (float) (startIndex) / (values.length - 1);
        float x2 = (float) (endIndex) / (values.length -1);
        float y1 = values[startIndex];
        float y2 = values[endIndex];
        float result = linInterp(x1, x2, y1, y2);

        return result;
    }

    private float linInterp(float x1, float x2, float y1, float y2) {
        //linear interpolation to estimate the value given the exceedance

        double slp = (y2 - y1) / (x2 - x1);
        double interpValue = slp * (this._percentile -x1) + y1;
        return (float) interpValue;
    }
}
