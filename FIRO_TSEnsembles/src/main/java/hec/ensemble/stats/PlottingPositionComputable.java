package hec.ensemble.stats;

import org.apache.commons.lang.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class PlottingPositionComputable implements MultiComputable, PlottingMethod {
    PlottingType method;
    Float[] reverseValues;
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

    public float[] computeProbability(float[] values) {
        orderValues(values);
        return getMethod(method).computeProbability(values);
    }

    public float[] orderValues(float[] values) {
        reverseValues = ArrayUtils.toObject(values);
        Arrays.sort(reverseValues, Collections.reverseOrder());
        return ArrayUtils.toPrimitive(reverseValues);
    }

    public Map<Float, Float> assignProbability(float[] value, float[] probability) {
        Float[] prob = ArrayUtils.toObject(probability);
        Float[] val = ArrayUtils.toObject(value);
        Map<Float, Float> probVal = new TreeMap<>();

        for(int i = 0; i < value.length; i++) {
            probVal.put(val[i], prob[i]);
        }
        return probVal;
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.PLOTTINGPOSITION};
    }

    @Override
    public String StatisticsLabel() {
        return "PLOTTING POSITION";
    }

}
