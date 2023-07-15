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

        Computable test = new NDayMultiComputable(cumulativeComputable, 1);
        Configurable c2 = (Configurable) test;
        c2.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1), ""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float results = test.compute(num1);
        assertEquals(13.057, results, .001);
    }

    @Test
    public void testNDayMultiComputableAcrossTimeTwoValues() {
        MultiComputable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {1,2});
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float[] results = test.multiCompute(num1);
        assertEquals(13.05785, results[0], 0.001);
        assertEquals(25.20661, results[1], 0.001);
    }


    @Test
    void testNDayMultiComputableAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new NDayMultiComputable(new CumulativeComputable(), 14);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-280.8352, output[3], 0.001);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    void testNDayMultiComputableAcrossTimeEnsemble2() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new NDayMultiComputable(new CumulativeComputable(), 13);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-698.4583, output[8], 0.001);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
