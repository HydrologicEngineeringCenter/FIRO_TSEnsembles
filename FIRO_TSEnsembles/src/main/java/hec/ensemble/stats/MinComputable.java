package hec.ensemble.stats;

public class MinComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private String outputUnit;
    private Configuration config;
    @Override
    public float compute(float[] values){
        java.util.Arrays.sort(values);
        return values[0];
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
        return new Statistics[]{Statistics.MIN};
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