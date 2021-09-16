package hec.stats;


import hec.JdbcDatabase;
import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeanComputableTest {
    @Test
    public void testMeanComputeSimpleArray() {
        MeanComputable test = new MeanComputable();
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(4.5, results);
    }
    @Test
    public void testMeanComputeSimpleArrayTens() {
        MeanComputable test = new MeanComputable();
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(43, results);
    }
    @Test
    public void testMeanWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new MeanComputable();
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.9668081998825073, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testMeanWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new MeanComputable();
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(-10.0833740234375, output[3]);
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
        EnsembleDatabase db = new JdbcDatabase(fn, JdbcDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
        // --- READ
        RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
        List<ZonedDateTime> issueDates = ets.getIssueDates();
        return db.getEnsemble(tsid, issueDates.get(0));
    }

}
