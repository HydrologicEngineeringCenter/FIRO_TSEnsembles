package hec.ensemble.stats;

import hec.ensemble.Ensemble;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class PlottingPositionTest {
    private Map<Float, Float> weibullMap() {
        Map<Float, Float> map = new TreeMap<>();
        map.put(0.16666f, 50.0f);
        map.put(0.33333f, 40.0f);
        map.put(0.5f, 30.0f);
        map.put(0.66666f, 20.0f);
        map.put(0.83333f, 10.0f);

        return map;
    }

    private Map<Float, Float> medianMap() {
        Map<Float, Float> map = new TreeMap<>();
        map.put(0.12963f, 50.0f);
        map.put(0.314815f, 40.0f);
        map.put(0.5f, 30.0f);
        map.put(0.685185f, 20.0f);
        map.put(0.87037f, 10.0f);

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

        Map<Float, Float> compareWeibull = weibull.assignProbability(weibullResults, weibullReverseValues);
        List<Float> weibullList = new ArrayList<>();
        for(Map.Entry<Float, Float> entry : compareWeibull.entrySet()) {
            weibullList.add(entry.getKey());
        }

        Map<Float, Float> truthWeibull = weibullMap();  // prob, value
        List<Float> weibullListTrue = new ArrayList<>();
        for(Map.Entry<Float, Float> entry : weibullMap().entrySet()) {
            weibullListTrue.add(entry.getKey());
        }

        Map<Float, Float> compareMedian = median.assignProbability(medianResults, medianReverseValues);
        List<Float> medianList = new ArrayList<>();
        for(Map.Entry<Float, Float> entry : compareMedian.entrySet()) {
            medianList.add(entry.getKey());
        }

        Map<Float, Float> truthMedian = medianMap();
        List<Float> medianListTrue = new ArrayList<>();
        for(Map.Entry<Float, Float> entry : truthMedian.entrySet()) {
            medianListTrue.add(entry.getKey());
        }


        assertEquals(weibullListTrue.get(2), weibullList.get(2), 4);
        assertEquals(truthWeibull.containsKey(0.5f), compareWeibull.containsKey(0.5f));

        assertEquals(medianListTrue.get(0), medianList.get(0), 4);
        assertEquals(truthMedian.containsKey(0.5f), compareMedian.containsKey(0.5f));
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
