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

class CumulativeFlowComputableTest {
    @Test
    public void testCumulativeFlowComputeSimpleArray() {
        Computable test = new CumulativeFlow("cfs");
        Configurable configTest = (Configurable)test;
        configTest.configure(new EnsembleConfiguration(null,null,null,"cfs"));
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(36, results);
    }
    @Test
    public void testCumulativeFlowWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new CumulativeFlow("kcfs");
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(57.041683197021484, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testCumulativeFlowWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new CumulativeFlow("kcfs");
            float[] output = e.iterateForTracesAcrossTime(test);//what does this even mean?
            assertEquals(-3398.096923828125, output[3]);
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
