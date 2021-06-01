package hec.stats;

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
        if (this._percentile <= 0) {
            throw new ArithmeticException("Percentile must be greater than 0");
        }
        //sorts array descending
        for (int i = 0; i < values.length; i++) {
            for (int j = i + 1; j < values.length; j++) {
                if (values[i] < values[j]) {
                    float tmp = values[i];
                    values[i] = values[j];
                    values[j] = tmp;
                }
            }
        }
        //creates an array of probabilities - index position / n
        int indexPosition = 0;
        float result = 0;
        float[] exceedances = new float[values.length];
        for (int i = 0; i < values.length; i++) {
            exceedances[i] = ((float) i + 1) / values.length;
        }

        //searches for the index position in the exceedance array to create two points for a line
        for (int i = 0; i < exceedances.length; i++) {
            if (this._percentile < exceedances[0]) {
                throw new ArithmeticException("Percentile beyond available exceedance range.  Increase percentile or add more values to input");
            }
            if (exceedances[i] > this._percentile) {
                indexPosition = i;
                float x1 = exceedances[indexPosition - 1];
                float x2 = exceedances[indexPosition];
                float y1 = values[indexPosition -1];
                float y2 = values[indexPosition];
                result = linInterp(x1, x2, y1, y2);
                break;
            }
            if (exceedances[0] == this._percentile) {
                return values[0];
            }
            if (exceedances[exceedances.length-1] == this._percentile) {
                return values[values.length-1];
            }

        }
        return result;
    }

    private float linInterp(float x1, float x2, float y1, float y2) {
        //linear interpolation to estimate the value given the exceedance

        double slp = (y2 - y1) / (x2 - x1);
        double interpValue = slp * (this._percentile -x1) + y1;
        return (float) interpValue;
    }
}
