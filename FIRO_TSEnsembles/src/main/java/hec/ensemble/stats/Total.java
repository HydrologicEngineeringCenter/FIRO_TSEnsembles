package hec.ensemble.stats;

import javax.measure.IncommensurableException;
import javax.measure.Unit;

public class Total implements Computable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    Configuration config;
    private Unit<?> unit;

    /**
     * Instantiates a total flow computable object
     */

    public Total() {
    }

    @Override
    public float compute(float[] values) {
        double factor = getConversionFactor();

        float flowVol = 0;
        for (float Q : values) flowVol += factor * Q * config.getDuration().getSeconds();
        return flowVol;
    }

    private double getConversionFactor() {
        unit = ConvertUnits.convertStringUnits(getInputUnits());
        try {
            return ConvertUnits.getAccumulationConversionFactor(unit);
        } catch (IncommensurableException e) {
            throw new RuntimeException(e);
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
        return UnitsUtil.getOutputUnitString(unit);
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }

    @Override
    public String StatisticsLabel() {
        return "TOTAL";
    }
}
