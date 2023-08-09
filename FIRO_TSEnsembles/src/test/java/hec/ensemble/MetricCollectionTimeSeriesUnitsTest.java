package hec.ensemble;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.stats.Configurable;
import hec.ensemble.stats.CumulativeComputable;
import hec.ensemble.stats.MultiComputable;
import hec.ensemble.stats.NDayMultiComputable;
import hec.metrics.MetricCollectionTimeSeries;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class MetricCollectionTimeSeriesUnitsTest {
    private static final String _fn = TestingPaths.instance.getTempDir()+"/importCsvToDatabaseMutable.db";
    private static SqliteDatabase _db = null;
    private static File f;
    @BeforeAll
    static void prepareNewDatabase() throws Exception {
        //ensure no previous test db exists.
        f = new File(_fn);
        if (f.exists()){
            if (!f.delete()){
                Logger.logError("database failed to delete, some resource must be using " + _fn);
                fail();
            }
        }

        //identify resource cache for csv files, and set up a 3 day time window
        String cacheDir = TestingPaths.instance.getCacheDir();
        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(3);
        //read from resource cache
        CsvEnsembleReader csvReader = new CsvEnsembleReader(cacheDir);
        EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);
        SqliteDatabase db = new SqliteDatabase(_fn, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
        //write ensemble time series to database for use in other tests.
        db.write(ets);
        _db =  db;
    }

    @Test
    void testNDayMultiComputableAcrossTimeTwoValues() {
        MultiComputable test = new NDayMultiComputable(new CumulativeComputable(), new float[] {1,2});
        Configurable c = (Configurable) test;
        c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1),"cfs"));
        float[] num1 = {11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8,11,2,6,4,5,6,7,8};
        float[] results = test.multiCompute(num1);
        assertEquals("ACRE-FT", test.getOutputUnits());
    }

    @Test
    void iterateAcrossTimestepsOfEnsemblesWithMultiComputable() {
        try {
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            MultiComputable test = new NDayMultiComputable(new CumulativeComputable(), new float[]{1});
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(test);

            assertEquals("ACRE-FT", output.getUnits());

            //write result
            _db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @Test
    void iterateTracesOfEnsemblesWithMultiComputable() {
        try {
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            MultiComputable test = new CumulativeComputable();
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.iterateTracesOfEnsemblesWithMultiComputable(test);

            assertEquals("ACRE-FT", output.getUnits());

            //write result
            _db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    @AfterAll
    static void cleanUp() throws Exception {
        f.delete();
        _db.close();
    }
}
