package hec.ensemble.stats;

import java.util.Arrays;

public class MedianComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
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
    public String StatisticsLabel() {
        return "MEDIAN";
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }
}
