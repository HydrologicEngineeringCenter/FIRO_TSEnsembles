package hec.ensemble.stats;

public class MinComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private Configuration config;
    @Override
    public float compute(float[] values){
        java.util.Arrays.sort(values);
        return values[0];
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
        return "MIN";
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }
}