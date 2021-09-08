package hec.stats;


import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import hec.ensemble.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static hec.stats.MultiStat.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MultiStatComputableTest {

    @Test
    public void testMultiStatComputeSimpleArrayMinTwoStat() {
        MultiComputable test = new MultiStatComputable(new MultiStat[] {MIN, MEAN});
        float[] num = {1,2,3,4,5,6,7,8};
        float[] results = test.MultiCompute(num);
        assertEquals(1, results[0]);
        assertEquals(4.5, results[1]);

    }
    @Test
    public void testMultiStatComputeSimpleArrayTensMedianOneStat() {
        MultiComputable test = new MultiStatComputable(new MultiStat[] {MIN});
        float[] num = {10,30,45,80,50};
        float[] results = test.MultiCompute(num);
        assertEquals(10, results[0]);
    }
    @Test
    public void testMultiStatComputeSimpleArrayTensMedianThreeStat() {
        MultiComputable test = new MultiStatComputable(new MultiStat[] {MIN, MEAN, MAX});
        float[] num = {10,30,45,80,50};
        float[] results = test.MultiCompute(num);
        assertEquals(10, results[0]);
        assertEquals(43, results[1]);
        assertEquals(80, results[2]);
    }
    @Test
    public void testMultiStatWithEnsembleTimeAcrossTracesMin() {
        try {
            Ensemble e = TestData.getSampleEnsemble();
            MultiComputable test = new MultiStatComputable(new MultiStat[] {MIN, MEAN, MAX});
            float[][] output = e.multiComputeForTracesAcrossTime(test);
            float[] value1 = output[3];
            assertEquals(-4000, value1[0]);
            assertEquals(-10.0833740234375, value1[1]);
            assertEquals(11.159436225891113, value1[2]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
