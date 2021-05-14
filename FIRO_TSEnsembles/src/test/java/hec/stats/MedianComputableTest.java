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

class MedianComputableTest {
    @Test
    public void testMedianComputeSimpleArray() {
        Computable test = new MedianComputable();
        float[] num = {2,1,3,4,5,8,7,6};
        float results = test.compute(num);
        assertEquals(4.5, results);
    }
    @Test
    public void testMedianComputeSimpleArrayTens() {
        Computable test = new MedianComputable();
        float[] num = {10,30,50,45,50};
        float results = test.compute(num);
        assertEquals(45, results);
    }
    @Test
    public void testMedianWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new MedianComputable();
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.953000009059906, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testMedianWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new MedianComputable();
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(0.8479999899864197, output[3]);
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
