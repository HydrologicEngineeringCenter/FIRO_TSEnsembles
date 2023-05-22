package hec.ensemble.stats;

public class MeanComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
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