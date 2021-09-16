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
            Ensemble e = getEnsemble();
            SingleComputable test = new MaxOfMaximumsComputable();
            float output = e.singleComputeForEnsemble(test);
            assertEquals(44.81431579589844, output);
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
