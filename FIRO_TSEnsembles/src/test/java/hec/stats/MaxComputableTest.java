package hec.stats;


import hec.JdbcDatabase;
import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MaxComputableTest {
    @Test
    public void testMaxComputeSimpleArray() {
        Computable maxTest = new MaxComputable();
        float[] nums = {1,2,3,4,5,6,7,8,9};
        float result = maxTest.compute(nums);
        assertEquals(9, result);
    }
    @Test
    public void testMaxComputeSimpleArrayUnordered() {
        Computable maxTest = new MaxComputable();
        float[] nums = {10,30,45,80,50};
        float result = maxTest.compute(nums);
        assertEquals(80, result);
    }
    @Test
    public void testMaxWithEnsembleTimeAcrossTraces() {
        try {
            Ensemble e = getEnsemble();
            Computable maxTest = new MaxComputable();
            float[] output = e.iterateForTimeAcrossTraces(maxTest);
            assertEquals(1.1300690174102783, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testMaxWithEnsembleTracesAcrossTime() {
        try {
            Ensemble e = getEnsemble();
            Computable maxTest = new MaxComputable();
            float[] output = e.iterateForTracesAcrossTime(maxTest);
            assertEquals(11.159436225891113, output[3]);
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
