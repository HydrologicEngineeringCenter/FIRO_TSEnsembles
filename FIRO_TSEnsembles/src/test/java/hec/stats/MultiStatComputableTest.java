package hec.stats;

import hec.ensemble.*;
import org.junit.jupiter.api.Test;

import static hec.stats.Statistics.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MultiStatComputableTest {

    @Test
    public void testMultiStatComputeSimpleArrayMinTwoStat() {
        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, AVERAGE});
        float[] num = {1,2,3,4,5,6,7,8};
        float[] results = test.multiCompute(num);
        assertEquals(1, results[0]);
        assertEquals(4.5, results[1]);

    }
    @Test
    public void testMultiStatComputeSimpleArrayTensMedianOneStat() {
        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN});
        float[] num = {10,30,45,80,50};
        float[] results = test.multiCompute(num);
        assertEquals(10, results[0]);
    }
    @Test
    public void testMultiStatComputeSimpleArrayTensMedianThreeStat() {
        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, AVERAGE, MAX});
        float[] num = {10,30,45,80,50};
        float[] results = test.multiCompute(num);
        assertEquals(10, results[0]);
        assertEquals(43, results[1]);
        assertEquals(80, results[2]);
    }

    @Test
    public void testMultiStatComputeSimpleArrayTensMedianFiveStat() {
        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, AVERAGE, MAX, VARIANCE, STANDARDDEVIATION});
        float[] num = {10,30,45,80,50};
        float[] results = test.multiCompute(num);
        assertEquals(10, results[0]);
        assertEquals(43, results[1]);
        assertEquals(80, results[2]);
        assertEquals(670, results[3]);
        assertEquals(25.884357452392578, results[4]);
    }
    @Test
    public void testMultiStatWithEnsembleTimeAcrossTracesMin() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, AVERAGE, MAX});
            float[][] output = e.multiComputeForTracesAcrossTime(test);
            float[] value0 = output[0];
            float[] value1 = output[1];
            float[] value2 = output[2];
            assertEquals(-4000, value0[3]);
            assertEquals(-10.0833740234375, value1[3]);
            assertEquals(11.159436225891113, value2[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testSingleStatMultiStatIterateForTracesAcrossTimeStdev() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MultiStatComputable(new Statistics[] {STANDARDDEVIATION});
            float[] output = e.iterateForTracesAcrossTime(test);
            float value0 = output[2];
            assertEquals(163.4685821533203, value0);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testSingleStatMultiStatIterateForTracesAcrossTimeVariance() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MultiStatComputable(new Statistics[] {VARIANCE});
            float[] output = e.iterateForTracesAcrossTime(test);
            float value0 = output[2];
            assertEquals(26721.9765625, value0);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testSingleStatMultiStatIterateForTimeAcrossTracesStdev() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MultiStatComputable(new Statistics[] {STANDARDDEVIATION});
            float[] output = e.iterateForTimeAcrossTraces(test);
            float value0 = output[4];
            assertEquals(0.07682710886001587, value0);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testSingleStatMultiStatIterateForTimeAcrossTracesVariance() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MultiStatComputable(new Statistics[] {VARIANCE});
            float[] output = e.iterateForTimeAcrossTraces(test);
            float value0 = output[4];
            assertEquals(0.005902404896914959, value0);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
