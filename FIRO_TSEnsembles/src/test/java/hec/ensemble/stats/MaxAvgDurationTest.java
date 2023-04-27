package hec.ensemble.stats;


import hec.ensemble.*;
import hec.ensemble.stats.Computable;
import hec.ensemble.stats.Configurable;
import hec.ensemble.stats.MaxAvgDuration;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;


class MaxAvgDurationTest {
    @Test
    public void testMaxAvgDurationSimpleArray() {
        MaxAvgDuration test = new MaxAvgDuration(6);
        float[] num = {1,2,3,4,5,6,7,8};
        test.configure(new EnsembleConfiguration(null, null, Duration.ofHours(3),""));

        float results = test.compute(num);
        assertEquals(7.5, results);
    }
    @Test
    public void testMaxAvgDurationSimpleArrayTens() {
        Computable test = new MaxAvgDuration(4);
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(51.25, results);
    }
    @Test
    public void testMaxAvgDurationWithEnsembleTimeAcrossTraces() {
        double start = System.currentTimeMillis();
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MaxAvgDuration(2);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(1.041534423828125, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
        double totalSecond = (System.currentTimeMillis()-start) / 1000;
        System.out.println("write time: " + totalSecond + " seconds");
    }
    @Test
    public void testMaxAvgDurationWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MaxAvgDuration(2);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(11.124134063720703, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
