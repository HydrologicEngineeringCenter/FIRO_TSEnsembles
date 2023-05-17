package hec.ensemble.stats;

public class MeanComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private String outputUnit;
    private Configuration config;
    @Override
    public float compute(float[] values){
        //calculate the mean of values
        float sum = 0f;
        for (float value : values) {
            sum += value;
        }
        return sum / values.length;
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
        return new Statistics[]{Statistics.AVERAGE};
    }

    @Override
    public String StatisticsLabel() {
        return "AVERAGE";
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }
}