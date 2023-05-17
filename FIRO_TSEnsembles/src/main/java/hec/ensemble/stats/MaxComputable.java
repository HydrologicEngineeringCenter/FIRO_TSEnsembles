package hec.ensemble.stats;

public class MaxComputable implements Computable, Configurable {
    private static final String DEFAULT_INPUTS_UNITS = "cfs";
    private String outputUnit;
    private Configuration config;

    @Override
    public float compute(float[] values){
        //calculate the max of values
        int size= values.length;
        java.util.Arrays.sort(values);
        return values[size-1];
    }

    private void getInputUnits() {
        if(config == null || config.getUnits().isEmpty()) {
            outputUnit = DEFAULT_INPUTS_UNITS;
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