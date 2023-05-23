package hec.ensemble.stats;


import hec.ensemble.*;
import hec.ensemble.stats.Computable;
import hec.ensemble.stats.Configurable;
import hec.ensemble.stats.Total;
import org.junit.jupiter.api.Test;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class TotalComputableTest {
    @Test
    public void testTotalComputeSimpleArray() {
        Computable test = new Total();
        Configurable configTest = (Configurable)test;
        configTest.configure(new EnsembleConfiguration(null,null, Duration.ofHours(1),"cfs"));
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(2.9752, results, 0.001);
    }
    @Test
    public void testTotalWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new Total();
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(4.7141, output[3], 0.001, "untolerable");
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testTotalWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new Total();
            float[] output = e.iterateForTracesAcrossTime(test);//what does this even mean?
            assertEquals(-280.8352, output[3], 0.001, "untolerable");//TestData database does not properly set units.
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }



}
