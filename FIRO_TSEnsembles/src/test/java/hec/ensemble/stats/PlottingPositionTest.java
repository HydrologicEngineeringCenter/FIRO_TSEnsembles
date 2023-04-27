package hec.ensemble.stats;

import hec.ensemble.Ensemble;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import hec.ensemble.stats.*;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
class PlottingPositionTest {
    private Map<Float, Float> weibullMap() {
        Map<Float, Float> map = new TreeMap<>();
        map.put(50.0f, 0.16666f);
        map.put(40.0f, 0.33333f);
        map.put(30.0f, 0.5f);
        map.put(20.0f, 0.66666f);
        map.put(10.0f, 0.83333f);

        return map;
    }

    private Map<Float, Float> medianMap() {
        Map<Float, Float> map = new TreeMap<>();
        map.put(50.0f, 0.12963f);
        map.put(40.0f, 0.314815f);
        map.put(30.0f, 0.5f);
        map.put(20.0f, 0.685185f);
        map.put(10.0f, 0.87037f);

        return map;
    }
    @Test
    void testPlottingPositionSimpleArray() {
        MultiComputable weibull = new PlottingPositionComputable(PlottingType.WEIBULL);
        MultiComputable median = new PlottingPositionComputable(PlottingType.MEDIAN);
        float[] num = {1,2,3,4,5,6,7,8};

        float[] weibullResults = weibull.multiCompute(num);
        float[] medianResults = median.multiCompute(num);
        assertEquals(.3333, weibullResults[2], 4);
        assertEquals(.44047, medianResults[3], 4);
    }

    @Test
    void testPlottingPositionSimpleMap() {
        PlottingPositionComputable weibull = new PlottingPositionComputable(PlottingType.WEIBULL);
        PlottingPositionComputable median = new PlottingPositionComputable(PlottingType.MEDIAN);

        float[] num = {10,30,20,50,40};

        float[] weibullResults = weibull.multiCompute(num);  //computing probability position
        float[] weibullReverseValues = weibull.orderValues(num);
        float[] medianResults = median.multiCompute(num);  //computing probability position
        float[] medianReverseValues = median.orderValues(num);

        Map<Float, Float> compareWeibull = weibull.assignProbability(weibullReverseValues, weibullResults);
        Map<Float, Float> truthWeibull = weibullMap();

        Map<Float, Float> compareMedian = median.assignProbability(medianReverseValues, medianResults);
        Map<Float, Float> truthMedian = medianMap();

        assertEquals(truthWeibull.get(40f), compareWeibull.get(40f), 4);
        assertEquals(truthWeibull.get(10f), compareWeibull.get(10f), 4);
        assertEquals(truthWeibull.containsValue(0.5f), compareWeibull.containsValue(0.5f));

        assertEquals(truthMedian.get(40f), compareMedian.get(40f), 4);
        assertEquals(truthMedian.get(10f), compareMedian.get(10f), 4);
        assertEquals(truthMedian.containsValue(0.5f), compareMedian.containsValue(0.5f));
    }

    @Test
    void testPlottingPositionEnsembleTracesAcrossTime() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable firstCompute = new MaxAvgDuration(2);
            float[] firstComputeOutput = e.iterateForTracesAcrossTime(firstCompute);

            PlottingPositionComputable weibull = new PlottingPositionComputable(PlottingType.WEIBULL);
            float[] weibullResults = weibull.multiCompute(firstComputeOutput);

            PlottingPositionComputable median = new PlottingPositionComputable(PlottingType.MEDIAN);
            float[] medianResults = median.multiCompute(firstComputeOutput);

            assertEquals(0.0166, weibullResults[0], 4);
            assertEquals(0.9833, weibullResults[58], 4);

            assertEquals(0.01198, medianResults[0], 4);
            assertEquals(0.98801, medianResults[58], 4);

        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
