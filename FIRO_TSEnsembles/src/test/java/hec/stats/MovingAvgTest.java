package hec.stats;

import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.Ensemble;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


class MovingAvgTest {

    private float[] testCases(int i) {
        float[] arr1 = {1,2,3,4,5,6,7,8,9,10,11};
        float[] arr2 = {11,30,46,80,51,91,107,72,70, 80, 30, 100, 104, 60};
        float[] arr3 = {11,30,46,80,51,91,107,72,70, 80, 29, 100, 104, 61};
        float[][] cases = {arr1, arr2, arr3};
        return cases[i];
    }

    private float[] testResults(int i) {
        float[] result1 = {2,3,4,5,6,7,8,9,10};
        float[] result2 = {0, 29,52,59,74,83,90,83, 74, 60, 70, 78, 88, 0};
        float[] result3 = {0, 41.75f, 51.75f, 67.0f, 82.25f, 80.25f, 85.0f, 82.25f, 62.75f, 69.75f, 78.25f, 73.5f, 0, 0};
        float[] result4 = {20.5f, 38f, 63f, 65.5f, 71f, 99f, 89.5f, 71f, 75f, 54.5f, 64.5f, 102f, 82.5f, 0};
        float[][] results = {result1, result2, result3, result4};
        return results[i];
    }

    @Test
    public void testMovingAvgExactSimpleArray2() {
        MovingAvg test = new MovingAvg(3,"Middle");
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),"hours"));
        assertArrayEquals(testResults(1), test.multiCompute(testCases(1)));
    }



    @Test
    public void testMovingAvgExactSimpleArray3() {
        MovingAvg test = new MovingAvg(4, "Middle");
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        assertArrayEquals(testResults(2), test.multiCompute(testCases(2)));
    }

    @Test
    public void testMovingAvgExactSimpleArray4() {
        MovingAvg test = new MovingAvg(4,"Middle");
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(2),""));
        assertArrayEquals(testResults(3), test.multiCompute(testCases(2)));
    }

    @Test
    public void testMovingAvgWithEnsembleforEachTrace() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new MovingAvg(3,"Middle");
            float[][] output = e.multiComputeForEachTraces(test);
            float[] output1 = output[0];
            assertEquals(-2066.6667f, output1[1]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testMovingAvgWithEnsembleForEachTrace6() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new MovingAvg(4,"Middle");
            float[][] output = e.multiComputeForEachTraces(test);
            float[] output1 = output[6];
            assertEquals(-1749.267333984375, output1[1]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
