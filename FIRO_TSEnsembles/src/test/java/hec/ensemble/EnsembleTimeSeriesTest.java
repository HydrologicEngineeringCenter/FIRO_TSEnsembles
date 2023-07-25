package hec.ensemble;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.stats.*;
import hec.metrics.MetricCollectionTimeSeries;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static hec.ensemble.stats.Statistics.*;
import static org.junit.jupiter.api.Assertions.*;

class EnsembleTimeSeriesTest {
    private static String _fn = TestingPaths.instance.getTempDir()+"/importCsvToDatabaseMutable.db";
    private static SqliteDatabase _db = null;
    @BeforeAll
    static void prepareNewDatabase() throws Exception {
        //ensure no previous test db exists.
        File f = new File(_fn);
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
    void iterateAcrossTimestepsOfEnsemblesWithMultiComputable() {
        try {
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, AVERAGE, MAX});
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(test);
            //verify at the data is properly computing for a set of known values
            float[] value0 = output.iterator().next().getValues()[0];
            float[] value1 = output.iterator().next().getValues()[1];
            float[] value2 = output.iterator().next().getValues()[2];
            assertEquals(0.953000009059906, value0[3]);//min
            assertEquals(0.9668087959289551, value1[3]);//mean
            assertEquals(1.1300690174102783, value2[3]);//max
            //write result
            _db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    void iterateAcrossTracesOfEnsemblesWithMultiComputable() {
        try {
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, AVERAGE, MAX});
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(test);
            //verify at the data is properly computing for a set of known values
            float[] value0 = output.iterator().next().getValues()[0];
            float[] value1 = output.iterator().next().getValues()[1];
            float[] value2 = output.iterator().next().getValues()[2];
            assertEquals(-4000, value0[3]);//min
            assertEquals(-10.0833740234375, value1[3]);//mean
            assertEquals(11.159436225891113, value2[3]);//max
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
            //verify at the data is properly computing for a set of known values
            float[] value0 = output.iterator().next().getValues()[0];
            float[] value1 = output.iterator().next().getValues()[1];
            float[] value2 = output.iterator().next().getValues()[2];
            assertEquals(-512.317932128906, value0[3],0.001);
            assertEquals(-165.0469970703125, value1[3],0.001);
            assertEquals(-247.69163513183594, value2[3],0.001);
            //write result
            _db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    void iterateAcrossTimestepsOfEnsemblesWithSingleComputable() {
        try {
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            Computable test = new MinComputable();
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithSingleComputable(test);
            //verify at the data is properly computing for a set of known values
            float[] value1 = output.iterator().next().getValues()[0];//get first ensemble issued.
            assertEquals(0.953000009059906, value1[3]); //get the 4th value in the array.
            //write result
            _db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    void iterateAcrossTracesOfEnsemblesWithSingleComputable() {
        try {
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            Computable test = new MinComputable();
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.iterateAcrossEnsembleTracesWithSingleComputable(test);
            //verify at the data is properly computing for a set of known values
            float[] value1 = output.iterator().next().getValues()[0];//get first ensemble issued.
            assertEquals(-4000, value1[3]); //get the 4th value in the array.
            //write result
            _db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @Test
    void computeSingleValueSummaryEnsembles() {
        try {
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            SingleComputable test = new MaxOfMaximumsComputable();
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.computeSingleValueSummary(test);
            //verify at the data is properly computing for a set of known values
            float[] value1 = output.iterator().next().getValues()[0];//get first ensemble issued.
            assertEquals(44.81431579589844, value1[0]); //get the 4th value in the array.
            //write result
            _db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @AfterAll
    static void cleanUp(){
        File f = new File(_fn);
        f.delete();
    }
}