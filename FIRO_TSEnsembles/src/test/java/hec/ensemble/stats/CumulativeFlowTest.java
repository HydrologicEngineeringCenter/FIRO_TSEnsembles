package hec.ensemble.stats;


import hec.ensemble.Ensemble;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import hec.ensemble.stats.CumulativeComputable;
import hec.ensemble.stats.MultiComputable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class CumulativeFlowTest {

    private float[] testCases(int i) {
        float[] arr1 = {1,2,3,4,5,6,7,8,9,10,11};
        float[] arr2 = {10,30,45,80,50,90,105,72,68};
        float[][] cases = {arr1, arr2};
        return cases[i];
    }

    private float[] testResults(int i) {
        float[] result1 = {1.0f,3.0f,6.0f,10.0f,15.0f,21.0f,28.0f,36.0f,45.0f,55.0f,66.0f};
        float[] result2 = {10, 40, 85, 165, 215, 305, 410, 482, 550};
        float[][] results = {result1, result2};
        return results[i];
    }

    @Test
    public void testCumulativeExactSimpleArray() {
        MultiComputable test = new CumulativeComputable();
        assertArrayEquals(testResults(0), test.multiCompute(testCases(0)));
    }

    @Test
    public void testCumulativeExactSimpleArrayTens() {
        MultiComputable test = new CumulativeComputable();
        assertArrayEquals(testResults(1), test.multiCompute(testCases(1)));
    }

    @Test
    public void testCumulativeWithEnsembleforEachTrace() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new CumulativeComputable();
            float[][] output = e.multiComputeForEachTraces(test);
            float[] output1 = output[0];
            assertEquals(-6199.046875, output1[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testCumulativeWithEnsembleForEachTrace6() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new CumulativeComputable();
            float[][] output = e.multiComputeForEachTraces(test);
            float[] output1 = output[6];
            assertEquals(-6990.3974609375, output1[10]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
