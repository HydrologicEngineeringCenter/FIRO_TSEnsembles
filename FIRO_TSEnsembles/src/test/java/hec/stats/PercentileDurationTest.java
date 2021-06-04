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


class PercentileDurationTest {
    @Test
    public void testPercentileExactSimpleArray() {
        PercentilesComputable test = new PercentilesComputable(1.0);
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(8, results);
    }
    @Test
    public void testPercentileInterpolateSimpleArray() {
        PercentilesComputable test = new PercentilesComputable(.30);
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(3.0999999046325684, results);
    }
    @Test
    public void testPercentileLowestSimpleArray() {
        PercentilesComputable test = new PercentilesComputable(0.0);
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(1, results);
    }
    @Test
    public void testPercentileHighestSimpleArray() {
        PercentilesComputable test = new PercentilesComputable(.125);
        float[] num = {1,2,3,4,5,6,7,8};
        float results = test.compute(num);
        assertEquals(1.875, results);
    }
    @Test
    public void testPercentileExactSimpleArrayTens() {
        PercentilesComputable test = new PercentilesComputable(.4);
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(39, results);
    }

    @Test
    public void testPercentileInterpolateSimpleArrayTens() {
        PercentilesComputable test = new PercentilesComputable(.5);
        float[] num = {10,30,45,80,50};
        float results = test.compute(num);
        assertEquals(45, results);
    }

    @Test
    public void testPercentileWithEnsembleTimeAcrossTracesLow() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new PercentilesComputable(0.05);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(0.953000009059906, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testPercentileWithEnsembleTimeAcrossTracesHigh() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new PercentilesComputable(0.95);
            float[] output = e.iterateForTimeAcrossTraces(test);
            assertEquals(1.1300690174102783, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testPercentileWithEnsembleTracesAcrossTimeLow() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new PercentilesComputable(0.05);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(0.6000000238418579, output[3]);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    public void testPercentileWithEnsembleTracesAcrossTimeHigh() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new PercentilesComputable(0.95);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(6.208317279815674, output[3]);
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