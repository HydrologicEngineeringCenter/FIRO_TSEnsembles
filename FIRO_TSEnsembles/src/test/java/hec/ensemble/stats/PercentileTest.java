package hec.ensemble.stats;


import hec.ensemble.*;
import hec.ensemble.stats.Computable;
import hec.ensemble.stats.MultiComputable;
import hec.ensemble.stats.PercentilesComputable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class PercentileTest {

    private float[] testCases(int i) {
        float[] arr1 = {1,2,3,4,5,6,7,8};
        float[] arr2 = {10,30,45,80,50};
        float[][] cases = {arr1, arr2};
        return cases[i];
    }

    private float testResults(int i) {
        float[] results = {8,3.0999999046325684f, 1, 1.875f, 39, 45};
        return results[i];
    }

    @Test
    public void testPercentileExactSimpleArray() {
        Computable test = new PercentilesComputable(1.0f);
        assertEquals(testResults(0), test.compute(testCases(0)));
    }

    @Test
    public void testPercentileInterpolateSimpleArray() {
        Computable test = new PercentilesComputable(.30f);
        assertEquals(testResults(1), test.compute(testCases(0)));
    }

    @Test
    public void testPercentileLowestSimpleArray() {
        Computable test = new PercentilesComputable(0.0f);
        assertEquals(testResults(2), test.compute(testCases(0)));
    }
    @Test
    public void testPercentileHighestSimpleArray() {
        Computable test = new PercentilesComputable(.125f);
        assertEquals(testResults(3), test.compute(testCases(0)));
    }
    @Test
    public void testPercentileExactSimpleArrayTens() {
        Computable test = new PercentilesComputable(.4f);
        assertEquals(testResults(4), test.compute(testCases(1)));
    }

    @Test
    public void testPercentileInterpolateSimpleArrayTens() {
        Computable test = new PercentilesComputable(.5f);
        assertEquals(testResults(5), test.compute(testCases(1)));
    }

    @Test
    public void testPercentileExactTwoValuesSimpleArray() {
        PercentilesComputable test = new PercentilesComputable(new float[] {1.0f, 0.0f});
        float[] num = {1,2,3,4,5,6,7,8};
        float[] results = test.multiCompute(num);
        assertEquals(8, results[0]);
        assertEquals(1, results[1]);
    }

    @Test
    public void testPercentileWithEnsembleTwoValuesEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new PercentilesComputable(new float[] {.05f, .95f});
            float[][] output = e.multiComputeForTracesAcrossTime(test);
            float[] value0 = output[0];
            float[] value1 = output[1];
            assertEquals(2, output.length);
            assertEquals(0.6000000238418579, value0[3]);
            assertEquals(6.208315849304199, value1[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testPercentileWithEnsembleThreeValuesEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new PercentilesComputable(new float[] {.05f, .5f, .95f});
            float[][] output = e.multiComputeForTracesAcrossTime(test);
            float[] value0 = output[0];
            float[] value1 = output[1];
            float[] value2 = output[2];
            assertEquals(0.6000000238418579, value0[3]);
            assertEquals(0.8479999899864197, value1[3]);
            assertEquals(6.208315849304199, value2[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testPercentileWithEnsembleTimeAcrossTracesLow() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new PercentilesComputable(0.05f);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.953000009059906, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testPercentileWithEnsembleTimeAcrossTracesHigh() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new PercentilesComputable(0.95f);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(1.1300690174102783, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testPercentileWithEnsembleTracesAcrossTimeLow() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new PercentilesComputable(0.05f);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(0.6000000238418579, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testPercentileWithEnsembleTracesAcrossTimeHigh() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new PercentilesComputable(0.95f);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(6.208315849304199, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
