package hec.ensemble;

import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.stats.*;
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


        String watershedName = "Kanektok";
        String suffix = "_hefs_csv_hourly";
        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(3);
        String cacheDir = TestingPaths.instance.getCacheDir(watershedName, issueDate1, suffix);
        //read from resource cache
        CsvEnsembleReader csvReader = new CsvEnsembleReader(cacheDir, suffix);
        EnsembleTimeSeries[] ets = csvReader.Read(watershedName, issueDate1, issueDate2);
        SqliteDatabase db = new SqliteDatabase(_fn, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
        //write ensemble time series to database for use in other tests.
        db.write(ets);
        _db =  db;
    }


    @Test
    public void testMetricCollectionAsTimeSeries_TwoStep() throws Exception {

        try {

            float[] nDayDuration = new float[]{2.0f};
            float targetPercentile = (float) 0.95;
            ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
            RecordIdentifier id = new hec.RecordIdentifier("Kanektok.BCAC1", "flow");
            EnsembleTimeSeries ets = _db.getEnsembleTimeSeries(id);

            //Step One
            MultiComputable cumulativeComputable = new CumulativeComputable();
            Computable cumulative = new NDayMultiComputable(cumulativeComputable, nDayDuration);

            //Step Two
            ComputableIndex percentileIndexCompute = new PercentileIndexComputable(targetPercentile);
            SingleTimeSeriesComputable twoStep = new TwoStepComputableSingleMetricTimeSeries(cumulative, percentileIndexCompute);
            MetricCollectionTimeSeries output = ets.computeSingleValueSummaryTimeSeries(twoStep);
            _db.write(output);

            //Get N-Day volumes for the first issue ensemble
            Ensemble e = ets.getEnsemble(issueDate1);
            Computable test = new NDayMultiComputable(new CumulativeComputable(), nDayDuration);
            float[] checkEnsembleVolumes = e.iterateForTracesAcrossTime(test);

            //find the exact percentile volume
            PercentilesComputable checkEnsemblePercentileVolume = new PercentilesComputable(targetPercentile);
            float[] checkExactPercentile = checkEnsemblePercentileVolume.multiCompute(checkEnsembleVolumes);

            //Convert first output hydrograph volume
            MultiComputable testOutput = new NDayMultiComputable(new CumulativeComputable(), nDayDuration);
            Configurable c = (Configurable) testOutput;
            c.configure(new EnsembleConfiguration(null, null, Duration.ofHours(1), ""));
            float[] value0 = output.iterator().next().getValues()[0];
            float[] results = testOutput.multiCompute(value0);

            // make sure metric collection is closest to what we want
            int startIndex = (int) (targetPercentile * (checkEnsembleVolumes.length - 1));
            int endIndex = startIndex + 1;

            if (Math.abs(checkExactPercentile[0] - checkEnsembleVolumes[startIndex]) < Math.abs(checkExactPercentile[0] - checkEnsembleVolumes[endIndex])) {
                assertEquals(results[0], checkEnsembleVolumes[startIndex]);
            } else {
                assertEquals(results[0], checkEnsembleVolumes[endIndex]);
            }

        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
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
