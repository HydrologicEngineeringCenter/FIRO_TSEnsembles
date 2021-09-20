package hec.stats;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MaxOfMaximumsComputableTest {
    @Test
    public void testMaximumOfMaximumsComputeSimpleArray() {
        SingleComputable test = new MaxOfMaximumsComputable();
        float[] num1 = {1,2,3,4,5,6,7,8};
        float[] num2 = {1,2,3,4,5,6,7,9};
        float[][] vals = {num1,num2};
        float results = test.compute(vals);
        assertEquals(9, results);
    }
    @Test
    public void testMaximumOfMaximumsEnsemble() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            SingleComputable test = new MaxOfMaximumsComputable();
            float output = e.singleComputeForEnsemble(test);
            assertEquals(44.81431579589844, output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

}
