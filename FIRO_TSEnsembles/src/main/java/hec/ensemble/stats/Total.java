package hec.ensemble.stats;

import static hec.ensemble.stats.ConvertUnits.convertCfsAcreFeet;

public class Total implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    Configuration config;
    private String outputUnit;

    /**
     * Instantiates a total flow computable object
     */

    public Total() {
    }

    @Override
    public float compute(float[] values) {
        float factor = getConversionFactor();

        float flowVol = 0;
        for (float Q : values) flowVol += factor * Q;
        return flowVol;
    }

    private float getConversionFactor() {
        if(getInputUnits().equalsIgnoreCase("cfs")) {
            outputUnit = "acre-ft";
            return convertCfsAcreFeet((int) config.getDuration().getSeconds());
        } else {
            return 1;
        }
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
        return outputUnit;
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.TOTAL};
    }

    @Override
    public String StatisticsLabel() {
        return "TOTAL";
    }
}
