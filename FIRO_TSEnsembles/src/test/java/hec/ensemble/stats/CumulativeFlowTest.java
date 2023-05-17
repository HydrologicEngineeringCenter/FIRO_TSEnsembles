package hec.ensemble.stats;


import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import hec.ensemble.stats.Configurable;
import hec.ensemble.stats.CumulativeComputable;
import hec.ensemble.stats.MultiComputable;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


class CumulativeFlowTest {

    private float[] testCases(int i) {
        float[] arr1 = {1,2,3,4,5,6,7,8,9,10,11};
        float[] arr2 = {10,30,45,80,50,90,105,72,68};
        float[][] cases = {arr1, arr2};
        return cases[i];
    }

    private float[] testResults(int i) {
        //Results computed in excel
        float[] result1 = {0.0826f,0.2479f,0.4959f,0.8264f,1.2397f,1.7355f,2.3140f,2.9752f,3.7190f,4.5455f,5.4545f};
        float[] result2 = {0.8264f, 3.3058f, 7.0248f, 13.6364f, 17.7686f, 25.2066f, 33.8843f, 39.8347f, 45.4545f};
        float[][] results = {result1, result2};
        return results[i];
    }

    @Test
    public void testCumulativeExactSimpleArray() {
        MultiComputable test = new CumulativeComputable();
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),"cfs"));
        assertArrayEquals(testResults(0), test.multiCompute(testCases(0)), 0.001f);
    }

    @Test
    public void testCumulativeExactSimpleArrayTens() {
        MultiComputable test = new CumulativeComputable();
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),"cfs"));
        assertArrayEquals(testResults(1), test.multiCompute(testCases(1)), .001f);
    }

    @Test
    public void testCumulativeWithEnsembleforEachTrace() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new CumulativeComputable();
            float[][] output = e.multiComputeForEachTraces(test);
            float[] output1 = output[0];
            assertEquals(-512.3179321289062, output1[3]);
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
            assertEquals(-577.7190551757812, output1[10]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
