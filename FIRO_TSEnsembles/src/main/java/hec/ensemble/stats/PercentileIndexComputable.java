package hec.ensemble.stats;

import java.util.Arrays;

public class PercentileIndexComputable implements ComputableIndex, MultiComputable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private float[] selectedPercentiles;
    private Configuration config;

    /**
     * Instantiates a percentile computable object
     * @param percentile is expected to be in decimal
     */

    public PercentileIndexComputable(float percentile) {
        this.selectedPercentiles = new float[] {percentile};
    }
    //empty constructor added to satisfy <init>() function deserializing from XML with reflection
    public PercentileIndexComputable(){}

    public PercentileIndexComputable(float[] percentiles) {
        this.selectedPercentiles = percentiles;
    }

    @Override
    public int compute(float[] values) {

        ValueIndexPair[] pairs = new ValueIndexPair[values.length];

        for (int i = 0; i < values.length; i++){
            pairs[i] = new ValueIndexPair(values[i], i);
        }

        Arrays.sort(pairs);
        if(selectedPercentiles.length == 0) {
            throw new ArrayIndexOutOfBoundsException("Percentile(s) less than or equal to 1 must be entered in the text field");
        }
        return computePercentile(pairs, selectedPercentiles[0]);
    }

    @Override
    public float[] multiCompute(float[] values) {
        int size = this.selectedPercentiles.length;
        float[] result = new float[size];
        int i = 0;


        ValueIndexPair[] pairs = new ValueIndexPair[values.length];

        for (int ii = 0; ii < values.length; ii++){
            pairs[ii] = new ValueIndexPair(values[ii], ii);
        }

        Arrays.sort(pairs);

        for (float p: this.selectedPercentiles) {
            result[i] = computePercentile(pairs, p);
            i++;
        }
        return result;
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


    /**
     * computePercentile must be sorted.
     * @param pairs must be sorted
     * @param interpVal is the percentile value
     */

    private int computePercentile(ValueIndexPair[] pairs, float interpVal) {
        if (interpVal > 1.0) {
            throw new ArithmeticException("Percentile must be less than or equal to 1");
        }
        if (interpVal < 0) {
            throw new ArithmeticException("Percentile must be greater than or equal to 0");
        }

        if (interpVal == 0) {
            return pairs[0].originalIndex;
        } else {
            if (interpVal == 1.0) {
                return pairs[pairs.length - 1].originalIndex;
            } else {
                int startIndex = (int) (interpVal * (pairs.length-1));
                int endIndex = startIndex + 1;

                float x1 = (float) startIndex / (pairs.length - 1);
                float x2 = (float) endIndex / (pairs.length - 1);
                float y1 = pairs[startIndex].value;
                float y2 = pairs[endIndex].value;

                float closestMatch = LinearInterp.linInterp(x1, x2, y1, y2, interpVal);

                if (Math.abs(closestMatch - y1) < Math.abs(closestMatch - y2)) {
                    return pairs[startIndex].originalIndex;
                } else {
                    return pairs[endIndex].originalIndex;
                }
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

    // Helper class to store value and its original index
    private static class ValueIndexPair implements Comparable<ValueIndexPair> {
        float value;
        int originalIndex;

        ValueIndexPair(float value, int originalIndex) {
            this.value = value;
            this.originalIndex = originalIndex;
        }

        // Compare based on the value
        @Override
        public int compareTo(ValueIndexPair other) {
            return Float.compare(this.value, other.value);
        }
    }
}
