package hec.ensemble.stats;

public class MaxComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUTS_UNITS = "cfs";
    private Configuration config;

    @Override
    public float compute(float[] values){
        //calculate the max of values
        int size= values.length;
        java.util.Arrays.sort(values);
        return values[size-1];
    }

    private String getInputUnits() {
        if(config == null || config.getUnits().isEmpty()) {
            return DEFAULT_INPUTS_UNITS;
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
        return new Statistics[]{Statistics.MAX};
    }

    @Override
    public String StatisticsLabel() {
        return "MAX";
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }
}