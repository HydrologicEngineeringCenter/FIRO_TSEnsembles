package hec.stats;


import hec.JdbcTimeSeriesDatabase;
import hec.EnsembleDatabase;
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
            Ensemble e = getEnsemble();
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
            Ensemble e = getEnsemble();
            Computable test = new MinComputable();
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-4000, output[3]);
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
        EnsembleDatabase db = new JdbcTimeSeriesDatabase(fn, JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
        // --- READ
        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Kanektok.SCRN2", "flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
        List<ZonedDateTime> issueDates = ets.getIssueDates();
        return db.getEnsemble(tsid, issueDates.get(0));
    }

}
