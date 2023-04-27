package hec.ensemble.stats;


import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import hec.ensemble.stats.Computable;
import hec.ensemble.stats.Configurable;
import hec.ensemble.stats.MaxAccumDuration;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;


class MaxAccumDurationTest {
    @Test
    public void testMaxAccumDurationSimpleArray() {
        MaxAccumDuration test = new MaxAccumDuration(3);
        float[] num = {1,2,3,4,5,6,7,8};
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(3),""));

        float results = test.compute(num);
        assertEquals(8, results);
    }

    @Test
    public void testMaxAccumDurationSimpleArray2() {
        MaxAccumDuration test = new MaxAccumDuration(4);
        float[] num = {4,8,2,5,4,9,1,2,5,9};
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(2),""));

        float results = test.compute(num);
        assertEquals(14, results);
    }

    @Test
    public void testMaxAccumDurationSimpleArrayTens() {
        Computable test = new MaxAccumDuration(4);
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(205, results);
    }
    @Test
    public void testMaxAccumDurationWithEnsembleTimeAcrossTraces() {
        double start = System.currentTimeMillis();
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MaxAccumDuration(2);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(2.08306884765625, output[3]);
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
            assertEquals(22.248268127441406, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
