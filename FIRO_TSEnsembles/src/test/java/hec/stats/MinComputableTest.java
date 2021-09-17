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

class MinComputableTest {
    @Test
    public void testMinComputeSimpleArray() {
        Computable test = new MinComputable();
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(1, results);
    }
    @Test
    public void testMinComputeSimpleArrayTens() {
        Computable test = new MinComputable();
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(10, results);
    }
    @Test
    public void testMinWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MinComputable();
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.953000009059906, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testMinWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MinComputable();
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-4000, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
