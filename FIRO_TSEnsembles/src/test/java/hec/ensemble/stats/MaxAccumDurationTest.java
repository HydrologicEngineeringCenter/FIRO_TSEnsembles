package hec.ensemble.stats;


import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class MaxAccumDurationTest {
    @Test
    public void testMaxAccumDurationSimpleArray() {
        //result computed in excel
        MaxAccumDuration test = new MaxAccumDuration(3);
        float[] num = {1,2,3,4,5,6,7,8};
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(3),"cfs"));

        float results = test.compute(num);
        assertEquals(1.9834, results, 0.001);
    }

    @Test
    public void testMaxAccumDurationSimpleArray2() {
        //result computed in excel
        MaxAccumDuration test = new MaxAccumDuration(4);
        float[] num = {4,8,2,5,4,9,1,2,5,9};
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(2),"cfs"));

        float results = test.compute(num);
        assertEquals(2.3140, results, 0.001);
    }

    @Test
    public void testMaxAccumDurationSimpleArrayTens() {
        Computable test = new MaxAccumDuration(4);
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),"cfs"));
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(16.9420, results, 0.001);
    }

    @Test
    public void testMaxAccumDurationSimpleArrayTensPrecip() {
        Computable test = new MaxAccumDuration(3);
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),"in"));
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(175, results, 0.001);
    }
    @Test
    public void testMaxAccumDurationWithEnsembleTimeAcrossTraces() {
        double start = System.currentTimeMillis();
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MaxAccumDuration(2);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.172, output[3], .001);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
        double totalSecond = (System.currentTimeMillis()-start) / 1000;
        System.out.println("write time: " + totalSecond + " seconds");
    }
    @Test
    public void testMaxAccumDurationWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MaxAccumDuration(2);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(1.838, output[3], 0.001);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
