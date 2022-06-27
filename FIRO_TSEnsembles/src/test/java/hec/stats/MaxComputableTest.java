package hec.stats;

import hec.ensemble.*;
import hec.ensemble.stats.Computable;
import hec.ensemble.stats.MaxComputable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MaxComputableTest {
    @Test
    public void testMaxComputeSimpleArray() {
        Computable maxTest = new MaxComputable();
        float[] nums = {1,2,3,4,5,6,7,8,9};
        float result = maxTest.compute(nums);
        assertEquals(9, result);
    }
    @Test
    public void testMaxComputeSimpleArrayUnordered() {
        Computable maxTest = new MaxComputable();
        float[] nums = {10,30,45,80,50};
        float result = maxTest.compute(nums);
        assertEquals(80, result);
    }
    @Test
    public void testMaxWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable maxTest = new MaxComputable();
            float[] output = e.iterateForTimeAcrossTraces(maxTest);
            assertEquals(1.1300690174102783, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testMaxWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable maxTest = new MaxComputable();
            float[] output = e.iterateForTracesAcrossTime(maxTest);
            assertEquals(11.159436225891113, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
