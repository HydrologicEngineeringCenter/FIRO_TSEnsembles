package hec.ensemble.stats;

import hec.ensemble.*;
import hec.ensemble.stats.Computable;
import hec.ensemble.stats.MeanComputable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MeanComputableTest {
    @Test
    public void testMeanComputeSimpleArray() {
        MeanComputable test = new MeanComputable();
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(4.5, results);
    }
    @Test
    public void testMeanComputeSimpleArrayTens() {
        MeanComputable test = new MeanComputable();
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(43, results);
    }
    @Test
    public void testMeanWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MeanComputable();
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.9668081998825073, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testMeanWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MeanComputable();
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-10.0833740234375, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
