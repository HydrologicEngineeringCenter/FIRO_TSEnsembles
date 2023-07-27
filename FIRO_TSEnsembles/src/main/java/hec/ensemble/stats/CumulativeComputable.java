package hec.ensemble.stats;

import org.apache.commons.lang.NotImplementedException;

import static hec.ensemble.stats.ConvertUnits.convertCfsAcreFeet;

public class CumulativeComputable implements MultiComputable, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    private static final long DEFAULT_INPUT_DURATION = 3600;  //seconds

    private Configuration config;
    private String outputUnit;


    @Override
    public float[] multiCompute(float[] values) {
        float factor = getConversionFactor();

        float[] flowVol = new float[values.length];
        for (int i = 0; i < flowVol.length; i++) {
            if(i == 0) {
                flowVol[i] = factor * values[0];
            } else {
                flowVol[i] = flowVol[i - 1] + factor * values[i];
            }
        }
        return flowVol;
    }

    private float getConversionFactor() {
        if(getInputUnits().equalsIgnoreCase("cfs")) {
            outputUnit = "acre-ft";
            return convertCfsAcreFeet((int) getDuration());
        } else {
            throw new NotImplementedException("Converting '"+getInputUnits()+"' to a volume is not supported");
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
        if(config == null) {
            return DEFAULT_INPUT_DURATION;
        } else {
            return config.getDuration().getSeconds();
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
    public String StatisticsLabel() {
        return "CUMULATIVE";
    }
}
