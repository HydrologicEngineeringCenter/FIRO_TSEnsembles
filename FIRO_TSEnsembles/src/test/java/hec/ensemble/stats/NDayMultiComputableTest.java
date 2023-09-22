package hec.ensemble.stats;

import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleConfiguration;
import hec.ensemble.Logger;
import hec.ensemble.TestData;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class NDayMultiComputableTest {
    @Test
    void testNDayMultiComputableAcrossTimeSimpleArray() {
        CumulativeComputable cumulativeComputable = new CumulativeComputable();
        ((Configurable) cumulativeComputable).configure(new EnsembleConfiguration(null, null, Duration.ofHours(1), "cfs"));

        Computable test = new NDayMultiComputable(cumulativeComputable, new float[]{1});
        Configurable c2 = (Configurable) test;
        c2.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1), ""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float results = test.compute(num1);
        assertEquals(12.1486, results, .001);
    }

    @Test
    void testNDayMultiComputableAcrossTimeTwoValues() {
        MultiComputable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {1,2});
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float[] results = test.multiCompute(num1);
        assertEquals(12.1486, results[0], 0.001);
        assertEquals(24.29737, results[1], 0.001);
    }

    @Test
    void testNDayMultiComputableAcrossTimeInterpolateDays() {
        MultiComputable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {0.5f});
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float[] results = test.multiCompute(num1);
        //Expected value computed in Excel. The first value in array is Time zero.
        assertEquals(5.950377599, results[0], 0.001);
    }

    @Test
    void testNDayMultiComputableAcrossTimeInterpolateDaysFraction() {
        MultiComputable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {0.4f});
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float[] results = test.multiCompute(num1);
        //Expected value computed in Excel. The first value in array is Time zero.
        assertEquals(5.0578, results[0], 0.001);
    }

    @Test
    void testNDayMultiComputableAcrossTimeInterpolateDaysWithMinutesTimeStep() {
        MultiComputable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {0.5f});
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofMinutes(30),""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float[] results = test.multiCompute(num1);
        //Expected value computed in Excel. The first value in array is Time zero.
        assertEquals(6.0743781, results[0], 0.001);
    }


    @Test
    void testNDayMultiComputableAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {14});
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-281.0137, output[3], 0.01);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    void testNDayMultiComputableAcrossTimeEnsemble2() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {13});
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-698.64, output[8], 0.01);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
