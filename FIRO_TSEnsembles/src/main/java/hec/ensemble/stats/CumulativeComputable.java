package hec.ensemble.stats;

import javax.measure.IncommensurableException;
import javax.measure.Unit;

public class CumulativeComputable implements MultiComputable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private Configuration config;
    private Unit<?> unit = null;

    @Override
    public float[] multiCompute(float[] values) {
        double factor = getConversionFactor();
        long duration = getDuration();

        float[] flowVol = new float[values.length];
        for (int i = 0; i < flowVol.length; i++) {
            if (i == 0) {
                flowVol[i] = (float) factor * (values[0]) * duration;
            } else {
                flowVol[i] = (float) (flowVol[i - 1] + factor * (values[i]) * duration);
            }
        }
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

    private long getDuration() {
        return config.getDuration().getSeconds();
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
        return "CUMULATIVE";
    }
}
