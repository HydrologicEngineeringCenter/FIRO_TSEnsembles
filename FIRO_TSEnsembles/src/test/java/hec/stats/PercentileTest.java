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


class PercentileTest {
    @Test
    public void testPercentileExactSimpleArray() {
        Computable test = new PercentilesComputable(1.0f);
        float[] num = {1,2,3,4,5,6,7,8};
        assertEquals(8, test.compute(num));
    }

    @Test
    public void testPercentileExactTwoValuesSimpleArray() {
        PercentilesComputable test = new PercentilesComputable(new float[] {1.0f, 0.0f});
        float[] num = {1,2,3,4,5,6,7,8};
        Double[] results = test.computeMulti(num);
        assertEquals(8, results[0]);
        assertEquals(1, results[1]);
    }

    @Test
    public void testPercentileInterpolateSimpleArray() {
        Computable test = new PercentilesComputable(.30f);
        float[] num = {1,2,3,4,5,6,7,8};
        assertEquals(3.0999999046325684, test.compute(num));
    }
    @Test
    public void testPercentileLowestSimpleArray() {
        Computable test = new PercentilesComputable(0.0f);
        float[] num = {1,2,3,4,5,6,7,8};
        assertEquals(1, test.compute(num));
    }
    @Test
    public void testPercentileHighestSimpleArray() {
        Computable test = new PercentilesComputable(.125f);
        float[] num = {1,2,3,4,5,6,7,8};
        assertEquals(1.875, test.compute(num));
    }
    @Test
    public void testPercentileExactSimpleArrayTens() {
        Computable test = new PercentilesComputable(.4f);
        float[] num = {10,30,45,80,50};
        assertEquals(39, test.compute(num));
    }

    @Test
    public void testPercentileInterpolateSimpleArrayTens() {
        Computable test = new PercentilesComputable(.5f);
        float[] num = {10,30,45,80,50};
        assertEquals(45, test.compute(num));
    }

    @Test
    public void testPercentileWithEnsembleTimeAcrossTracesLow() {
        try {
            Ensemble e = getEnsemble();
            Computable test = new PercentilesComputable(0.05f);
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
            Computable test = new PercentilesComputable(0.95f);
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
            Computable test = new PercentilesComputable(0.05f);
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
            Computable test = new PercentilesComputable(0.95f);
            float[] output = e.iterateForTracesAcrossTime(test);
            assertEquals(6.208315849304199, output[3]);
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
