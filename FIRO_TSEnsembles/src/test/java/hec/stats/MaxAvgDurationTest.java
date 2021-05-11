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

class MaxAvgDurationTest {
    @Test
    public void testMaxAvgDurationSimpleArray() {
        Computable test = new MaxAvgDuration(2);
        float[] num = {1,2,3,4,5,6,7,8};

        float results = test.compute(num);
        assertEquals(7.5, results);
    }
    @Test
    public void testMaxAvgDurationSimpleArrayTens() {
        Computable test = new MaxAvgDuration(4);
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(51.25, results);
    }
    @Test
    public void testMaxAvgDurationWithEnsembleTimeAcrossTraces() {
        double start = System.currentTimeMillis();
        try {
            Ensemble e = getEnsemble();
            Computable test = new MaxAvgDuration(2);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(1.041534423828125, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
        double totalSecond = (System.currentTimeMillis()-start) / 1000;
        System.out.println("write time: " + totalSecond + " seconds");
    }
    @Test
    public void testMaxAvgDurationWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new MaxAvgDuration(2);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(11.124134063720703, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    private Ensemble getEnsemble() throws Exception {
        String fn = TestingPaths.instance.getTempDir() + "/importCsvToDatabase.db";
        File f = new File(fn);
        if(!f.exists()) {
            DatabaseGenerator.createTestDatabase(fn, 1);
        }
        TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fn, JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
        // --- READ
        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Kanektok.SCRN2", "flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
        List<ZonedDateTime> issueDates = ets.getIssueDates();
        return db.getEnsemble(tsid, issueDates.get(0));
    }
}
