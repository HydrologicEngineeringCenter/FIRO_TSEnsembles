package hec.ensemble.stats;

import java.util.Arrays;

public class MedianComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private String outputUnit;
    private Configuration config;

    @Override
    public float compute(float[] values) {
        int n = values.length;
        Arrays.sort(values);
        if(n % 2 !=0) {
            return values[(n) /2];
        }
        return (values[(n)/2-1] + values[n /2]) / 2;
    }

    private void getInputUnits() {
        if(config == null || config.getUnits().isEmpty()) {
            outputUnit = DEFAULT_INPUT_UNITS;
        } else {
            outputUnit = config.getUnits();
        }
    }

    @Override
    public String getOutputUnits() {
        getInputUnits();
        return outputUnit;
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MEDIAN};
    }

    @Override
    public String StatisticsLabel() {
        return "MEDIAN";
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }
}
