package hec.stats;


import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import hec.ensemble.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MeanComputableTest {
    @Test
    public void testMeanCompute() {
        MeanComputable test = new MeanComputable();
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(4.5, results);
    }
    @Test
    public void testMeanCompute2() {
        MeanComputable test = new MeanComputable();
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(43, results);
    }
    @Test
    public void testMeanWithEnsemble() {
        try {
            String fn = TestingPaths.instance.getTempDir()+"/importCsvToDatabase.db";
            File f = new File(fn);
            f.delete();

            DatabaseGenerator.createTestDatabase(fn,1);
            TimeSeriesDatabase db  =new JdbcTimeSeriesDatabase(fn, JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
            // --- READ
            TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Kanektok.SCRN2","flow");
            EnsembleTimeSeries ets =  db.getEnsembleTimeSeries(tsid);
            List<ZonedDateTime> issueDates = ets.getIssueDates();
            Ensemble e = db.getEnsemble(tsid, issueDates.get(1));
            Computable test = new MeanComputable();
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.9668081998825073, output[3]);


        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
}
