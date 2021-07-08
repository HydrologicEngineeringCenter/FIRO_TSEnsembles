package hec.stats;


import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import hec.ensemble.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MultiStatComputableTest {
    @Test
    public void testMultiStatComputeSimpleArrayMin() {
        Computable test = new MultiStatComputable().createCalculation(MultiStat.MIN);
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(1, results);
    }
    @Test
    public void testMultiStatComputeSimpleArrayTensMedian() {
        Computable test = new MultiStatComputable().createCalculation(MultiStat.MEDIAN);
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(45, results);
    }
    @Test
    public void testMultiStatWithEnsembleTimeAcrossTracesMin() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MultiStatComputable().createCalculation(MultiStat.MIN);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.953000009059906, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testMultiStatWithEnsembleTracesAcrossTimeMin() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            Computable test = new MultiStatComputable().createCalculation(MultiStat.MIN);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-4000, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
