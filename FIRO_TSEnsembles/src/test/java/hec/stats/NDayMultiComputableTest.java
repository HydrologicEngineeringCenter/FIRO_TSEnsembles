package hec.stats;

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
    public void testNDayMultiComputableAcrossTimeSimpleArray() {
        Computable test = new NDayMultiComputable(new CumulativeComputable(), 1);
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),""));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float results = test.compute(num1);
        assertEquals(158, results);
    }


    @Test
    public void testNDayMultiComputableAcrossTimeEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new NDayMultiComputable(new CumulativeComputable(), 14);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-3398.096923828125, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    public void testNDayMultiComputableAcrossTimeEnsemble2() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new NDayMultiComputable(new CumulativeComputable(), 13);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-8451.3525390625, output[8]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
