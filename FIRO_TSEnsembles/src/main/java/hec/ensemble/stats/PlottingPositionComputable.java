package hec.ensemble.stats;

import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class PlottingPositionComputable implements MultiComputable, PlottingMethod, Configurable {
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    PlottingType method;
    Float[] reverseValues;
    private Configuration config;
    private String outputUnit;

    public PlottingPositionComputable(PlottingType method) {
        this.method = method;
    }

    /**
     * @return array of plotting position probabilities.
     * {@link hec.ensemble.Ensemble#iterateForTimeAcrossTraces(Computable)} Used after metrics are computed with computeForTracesAcrossTime
     * Current implementation only computes Weibull plotting position
     */
    @Override
    public float[] multiCompute(float[] values) {
        return computeProbability(values);
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

    public float[] computeProbability(float[] values) {
        orderValues(values);
        return getMethod(method).computeProbability(values);
    }

    public float[] orderValues(float[] values) {
        reverseValues = ArrayUtils.toObject(values);
        Arrays.sort(reverseValues, Collections.reverseOrder());
        return ArrayUtils.toPrimitive(reverseValues);
    }

    public Map<Float, Float> assignProbability(float[] probability, float[] value) {
        Float[] prob = ArrayUtils.toObject(probability);
        Float[] val = ArrayUtils.toObject(value);
        Map<Float, Float> probVal = new TreeMap<>();

        for(int i = 0; i < value.length; i++) {
            probVal.put(prob[i], val[i]);
        }
        return probVal;
    }

    @Override
    public String StatisticsLabel() {
        return "PLOTTING POSITION";
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }
}
