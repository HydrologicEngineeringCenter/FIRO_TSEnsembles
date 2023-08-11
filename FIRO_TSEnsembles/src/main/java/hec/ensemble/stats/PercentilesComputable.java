package hec.ensemble.stats;

import java.util.Arrays;

public class PercentilesComputable implements Computable, MultiComputable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private float[] selectedPercentiles;
    private Configuration config;

    /**
     * Instantiates a percentile computable object
     * @param percentile is expected to be in decimal
     */

    public PercentilesComputable(float percentile) {
        this.selectedPercentiles = new float[] {percentile};
    }
    //empty constructor added to satisfy <init>() function deserializing from XML with reflection
    public PercentilesComputable(){}

    public PercentilesComputable(float[] percentiles) {
        this.selectedPercentiles = percentiles;
    }

    @Override
    public float compute(float[] values) {
        Arrays.sort(values);
        return computePercentile(values, selectedPercentiles[0]);
    }

    private String getInputUnits() {
        if(config == null || config.getUnits().isEmpty()) {
            return DEFAULT_INPUT_UNITS;
        } else {
            return config.getUnits();
        }
    }

    @Override
    public String getOutputUnits() {
        return getInputUnits();
    }

    @Override
    public float[] multiCompute(float[] values) {
        int size = this.selectedPercentiles.length;
        float[] result = new float[size];
        int i = 0;
        Arrays.sort(values);

        for (float p: this.selectedPercentiles) {
            result[i] = computePercentile(values, p);
            i++;
        }
        return result;
    }
    /**
     * computePercentile must be sorted.
     * @param values must be sorted
     * @param interpVal is the percentile value
     */

    private float computePercentile(float[] values, float interpVal) {
        if (interpVal > 1.0) {
            throw new ArithmeticException("Percentile must be less than equal to 1");
        }
        if (interpVal < 0) {
            throw new ArithmeticException("Percentile must be greater than or equal to 0");
        }

        if (interpVal == 0) {
            return values[0];
        } else {
            if (interpVal == 1.0) {
                return values[values.length - 1];
            } else {
                int startIndex = (int) (interpVal * (values.length-1));
                int endIndex = startIndex + 1;

                float x1 = (float) startIndex / (values.length - 1);
                float x2 = (float) endIndex / (values.length - 1);
                float y1 = values[startIndex];
                float y2 = values[endIndex];

                return LinearInterp.linInterp(x1, x2, y1, y2, interpVal);
            }
        }
    }

    @Override
    public String StatisticsLabel() {
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < selectedPercentiles.length; i ++){
            if(i == selectedPercentiles.length-1){
                label.append(Statistics.PERCENTILES).append("(").append(selectedPercentiles[i]).append(")");
            }
            else{
                label.append(Statistics.PERCENTILES).append("(").append(selectedPercentiles[i]).append(")|");
            }
        }
        return label.toString();
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }
}
